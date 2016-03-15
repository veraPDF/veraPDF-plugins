package org.verapdf;

import au.edu.apsr.mtk.base.*;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.impl.VeraPDFMeta;
import com.adobe.xmp.impl.VeraPDFXMPNode;
import com.adobe.xmp.impl.XMPSchemaRegistryImpl;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractMetadataFeaturesExtractor;
import org.verapdf.features.MetadataFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class METSMetadataExtractor extends AbstractMetadataFeaturesExtractor {

    private XMPMeta dmdMeta = null;
    private XMPMeta rightsMeta = null;
    private XMPMeta techMeta = null;
    private XMPMeta sourceMeta = null;
    private XMPMeta digiprovMeta = null;

    @Override
    public List<FeatureTreeNode> getMetadataFeatures(MetadataFeaturesData metadataFeaturesData) {
        List<FeatureTreeNode> result = new ArrayList<>(1);
        try {
            File outFile = getOutFile();
            byte[] bytes = metadataFeaturesData.getStream();
            convertXMPToMETS(new ByteInputStream(bytes, bytes.length), new FileOutputStream(outFile));
            FeatureTreeNode node = FeatureTreeNode.createRootNode("resultFile");
            node.setValue(outFile.getCanonicalPath());
            result.add(node);
        } catch (XMPException | METSException | IOException | SAXException | FeatureParsingException | ParserConfigurationException e) {
            try {
                FeatureTreeNode node = FeatureTreeNode.createRootNode("Error");
                node.setValue(e.getMessage());
                result.add(node);
            } catch (FeatureParsingException e1) {
                throw new IllegalStateException(e1);
            }
        }
        return result;
    }

    private void convertXMPToMETS(InputStream toConvert, OutputStream out) throws XMPException, METSException, IOException, SAXException, ParserConfigurationException {
        VeraPDFMeta meta = VeraPDFMeta.parse(toConvert);
        divideMetas(meta);
        METSWrapper metsWrapper = new METSWrapper();
        createMETS(metsWrapper);
        metsWrapper.validate();
        metsWrapper.write(out);
    }

    private void createMETS(METSWrapper metsWrapper) throws METSException, SAXException, ParserConfigurationException, XMPException, IOException {
        METS mets = metsWrapper.getMETSObject();

        DmdSec dmd = mets.newDmdSec();
        MdWrap dmdWrap = dmd.newMdWrap();
        addXMPTreeToMdWrap(dmdWrap, this.dmdMeta);
        dmd.setMdWrap(dmdWrap);
        mets.addDmdSec(dmd);

        AmdSec amd = mets.newAmdSec();

        RightsMD rightsMD = amd.newRightsMD();
        MdWrap rightsWrap = rightsMD.newMdWrap();
        addXMPTreeToMdWrap(rightsWrap, this.rightsMeta);
        rightsMD.setMdWrap(rightsWrap);
        amd.addRightsMD(rightsMD);

        TechMD techMD = amd.newTechMD();
        MdWrap techWrap = techMD.newMdWrap();
        addXMPTreeToMdWrap(techWrap, this.techMeta);
        techMD.setMdWrap(techWrap);
        amd.addTechMD(techMD);

        SourceMD sourceMD = amd.newSourceMD();
        MdWrap sourceWrap = sourceMD.newMdWrap();
        addXMPTreeToMdWrap(sourceWrap, this.sourceMeta);
        sourceMD.setMdWrap(sourceWrap);
        amd.addSourceMD(sourceMD);

        DigiprovMD digiprovMD = amd.newDigiprovMD();
        MdWrap digiprovWrap = digiprovMD.newMdWrap();
        addXMPTreeToMdWrap(digiprovWrap, this.rightsMeta);
        digiprovMD.setMdWrap(digiprovWrap);
        amd.addDigiprovMD(digiprovMD);

        mets.addAmdSec(amd);
    }

    private void addXMPTreeToMdWrap(MdWrap wrap, XMPMeta meta) throws XMPException, ParserConfigurationException, IOException, SAXException {
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(meta, bstream);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(bstream.toByteArray()));
        wrap.setXmlData(doc.getDocumentElement());
    }

    private void divideMetas(VeraPDFMeta meta) {

        List<VeraPDFXMPNode> xmpProps = meta.getProperties();
        List<String[]> properties;

        if (meta.getExtensionSchemasNode() != null) {
            properties = new ArrayList<>(xmpProps.size() + 1);
            properties.add(new String[]{XMPSchemaRegistryImpl.NS_PDFA_EXTENSION, "schemas"});
        } else {
            properties = new ArrayList<>(xmpProps.size());
        }

        for (VeraPDFXMPNode node : xmpProps) {
            properties.add(new String[]{node.getNamespaceURI(), node.getName()});
        }

        this.dmdMeta = meta.getCloneOfInitialMeta();
        this.rightsMeta = meta.getCloneOfInitialMeta();
        this.techMeta = meta.getCloneOfInitialMeta();
        this.sourceMeta = meta.getCloneOfInitialMeta();
        this.digiprovMeta = meta.getCloneOfInitialMeta();

        boolean isDMDEmpty = true;
        boolean isRightsEmpty = true;
        boolean isTechEmpty = true;
        boolean isSourceEmpty = true;
        boolean isDIGIPROVEmpty = true;

        for (String[] el : properties) {
            switch (METSTypeRegistry.getTypeForProperty(el[0], el[1])) {
                case DMD_SEC:
                    isDMDEmpty = false;
                    rightsMeta.deleteProperty(el[0], el[1]);
                    techMeta.deleteProperty(el[0], el[1]);
                    sourceMeta.deleteProperty(el[0], el[1]);
                    digiprovMeta.deleteProperty(el[0], el[1]);
                    break;
                case RIGHTS_MD:
                    isRightsEmpty = false;
                    dmdMeta.deleteProperty(el[0], el[1]);
                    techMeta.deleteProperty(el[0], el[1]);
                    sourceMeta.deleteProperty(el[0], el[1]);
                    digiprovMeta.deleteProperty(el[0], el[1]);
                    break;
                case TECH_MD:
                    isTechEmpty = false;
                    dmdMeta.deleteProperty(el[0], el[1]);
                    rightsMeta.deleteProperty(el[0], el[1]);
                    sourceMeta.deleteProperty(el[0], el[1]);
                    digiprovMeta.deleteProperty(el[0], el[1]);
                    break;
                case SOURCE_MD:
                    isSourceEmpty = false;
                    dmdMeta.deleteProperty(el[0], el[1]);
                    rightsMeta.deleteProperty(el[0], el[1]);
                    techMeta.deleteProperty(el[0], el[1]);
                    digiprovMeta.deleteProperty(el[0], el[1]);
                    break;
                case DIGIPROV_MD:
                    isDIGIPROVEmpty = false;
                    dmdMeta.deleteProperty(el[0], el[1]);
                    rightsMeta.deleteProperty(el[0], el[1]);
                    techMeta.deleteProperty(el[0], el[1]);
                    sourceMeta.deleteProperty(el[0], el[1]);
                    break;
            }
        }

        if (isDMDEmpty) {
            this.dmdMeta = null;
        }
        if (isRightsEmpty) {
            this.rightsMeta = null;
        }
        if (isTechEmpty) {
            this.techMeta = null;
        }
        if (isSourceEmpty) {
            this.sourceMeta = null;
        }
        if (isDIGIPROVEmpty) {
            this.digiprovMeta = null;
        }
    }

    private File getOutFile() throws IOException {
        File temp = File.createTempFile("mets", ".xml");
        return temp;
    }

    @Override
    public String getID() {
        return "63f0a295-587b-4e50-909c-4b47e02f64f7";
    }

    @Override
    public String getDescription() {
        return "This extractor generates METS file based on XMP package.";
    }
}
