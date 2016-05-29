package org.verapdf;

import au.edu.apsr.mtk.base.*;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.impl.VeraPDFMeta;
import com.adobe.xmp.impl.VeraPDFXMPNode;
import com.adobe.xmp.impl.XMPSchemaRegistryImpl;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractMetadataFeaturesExtractor;
import org.verapdf.features.MetadataFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
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
            File outFile = getOutFile(result);
            byte[] bytes = metadataFeaturesData.getStream();
            convertXMPToMETS(new ByteArrayInputStream(bytes), new FileOutputStream(outFile));
            FeatureTreeNode node = FeatureTreeNode.createRootNode("resultFile");
            node.setValue(outFile.getCanonicalPath());
            result.add(node);
        } catch (XMPException | METSException | IOException | SAXException | FeatureParsingException | ParserConfigurationException e) {
            try {
                result.clear();
                FeatureTreeNode node = FeatureTreeNode.createRootNode("error");
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
        //TODO: fix mets creation in such way that validating will not generate any exceptions
//        metsWrapper.validate();
        metsWrapper.write(out);
    }

    private void createMETS(METSWrapper metsWrapper) throws METSException, SAXException, ParserConfigurationException, XMPException, IOException {
        METS mets = metsWrapper.getMETSObject();

        if (this.dmdMeta != null) {
            DmdSec dmd = mets.newDmdSec();
            dmd.setID("DMD_ID_1");
            MdWrap dmdWrap = dmd.newMdWrap();
            addXMPTreeToMdWrap(dmdWrap, this.dmdMeta);
            dmd.setMdWrap(dmdWrap);
            mets.addDmdSec(dmd);
        }

        if (this.rightsMeta != null || this.techMeta != null || this.sourceMeta != null || this.digiprovMeta != null) {
            AmdSec amd = mets.newAmdSec();

            if (this.rightsMeta != null) {
                RightsMD rightsMD = amd.newRightsMD();
                rightsMD.setID("RIGHTSMD_ID_1");
                MdWrap rightsWrap = rightsMD.newMdWrap();
                addXMPTreeToMdWrap(rightsWrap, this.rightsMeta);
                rightsMD.setMdWrap(rightsWrap);
                amd.addRightsMD(rightsMD);
            }

            if (this.techMeta != null) {
                TechMD techMD = amd.newTechMD();
                techMD.setID("TECHMD_ID_1");
                MdWrap techWrap = techMD.newMdWrap();
                addXMPTreeToMdWrap(techWrap, this.techMeta);
                techMD.setMdWrap(techWrap);
                amd.addTechMD(techMD);
            }

            if (this.sourceMeta != null) {
                SourceMD sourceMD = amd.newSourceMD();
                sourceMD.setID("SOURCEMD_ID_1");
                MdWrap sourceWrap = sourceMD.newMdWrap();
                addXMPTreeToMdWrap(sourceWrap, this.sourceMeta);
                sourceMD.setMdWrap(sourceWrap);
                amd.addSourceMD(sourceMD);
            }

            if (this.digiprovMeta != null) {
                DigiprovMD digiprovMD = amd.newDigiprovMD();
                digiprovMD.setID("DIGIPROVMD_ID_1");
                MdWrap digiprovWrap = digiprovMD.newMdWrap();
                addXMPTreeToMdWrap(digiprovWrap, this.digiprovMeta);
                digiprovMD.setMdWrap(digiprovWrap);
                amd.addDigiprovMD(digiprovMD);
            }

            mets.addAmdSec(amd);
        }
    }

    private void addXMPTreeToMdWrap(MdWrap wrap, XMPMeta meta) throws XMPException, ParserConfigurationException, IOException, SAXException {
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        XMPMetaFactory.serialize(meta, bstream);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(bstream.toByteArray()));
        wrap.setXmlData(doc.getDocumentElement());
        wrap.setMDType("OTHER");
        wrap.setOtherMDType("XMP");
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

    private File getOutFile(List<FeatureTreeNode> nodes) throws FeatureParsingException, IOException {
        METSConfig config = getConfig(nodes);
        if (config.getOutFolder() == null) {
            File tempFolder = getTempFolder();
            File res = getOutFileInFolder(tempFolder);
            return res;
        } else {
            File outFolder = new File(config.getOutFolder());
            if (outFolder.isDirectory()) {
                File res = getOutFileInFolder(outFolder);
                return res;
            } else {
                FeatureTreeNode node = FeatureTreeNode.createRootNode("error");
                node.setValue("Config file contains out folder path but it doesn't link a directory.");
                nodes.add(node);
                File tempFolder = getTempFolder();
                File res = getOutFileInFolder(tempFolder);
                return res;
            }
        }
    }

    private File getTempFolder() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempFolder = new File(tempDir, "veraPDFMETSPluginTemp");
        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        }
        return tempFolder;
    }

    private File getOutFileInFolder(File folder) throws IOException {
        return File.createTempFile("veraPDF_METS_Plugin_out", ".xml", folder);
    }

    private METSConfig getConfig(List<FeatureTreeNode> nodes) throws FeatureParsingException {
        METSConfig config = METSConfig.defaultInstance();
        File conf = getConfigFile();
        if (conf.isFile() && conf.canRead()) {
            try {
                config = METSConfig.fromXml(new FileInputStream(conf));
            } catch (JAXBException | FileNotFoundException e) {
                FeatureTreeNode node = FeatureTreeNode.createRootNode("error");
                node.setValue("Config file contains wrong syntax. Error message: " + e.getMessage());
                nodes.add(node);
            }
        }
        return config;
    }

    private File getConfigFile() {
        return new File(getFolderPath().toFile(), "config.xml");
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
