package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class EmbeddedFileSampleExtractor extends
        AbstractEmbeddedFileFeaturesExtractor {
    public static final String ID = "b99ad964-5d34-4b17-9bec-b19a174ae772";
    public static final String DESCRIPTION = "This sample Extractor generates custom features report containing data from incoming EmbeddedFileFeaturesData object.";

    private static final Logger LOGGER = Logger
            .getLogger(EmbeddedFileSampleExtractor.class);

    public EmbeddedFileSampleExtractor() {
	    super(ID, DESCRIPTION);
	}

    @Override
    public List<FeatureTreeNode> getEmbeddedFileFeatures(
            EmbeddedFileFeaturesData embeddedFileFeaturesData) {
        List<FeatureTreeNode> res = new ArrayList<>();
        try {
            FeatureTreeNode stream = FeatureTreeNode
                    .createRootNode("streamContent");
            stream.setValue(DatatypeConverter
                    .printHexBinary(embeddedFileFeaturesData.getStream()));
            res.add(stream);

            addObjectNode("checkSum", embeddedFileFeaturesData.getCheckSum(),
                    res);
            addObjectNode("creationDate",
                    formatXMLDate(embeddedFileFeaturesData.getCreationDate()),
                    res);
            addObjectNode("description",
                    embeddedFileFeaturesData.getDescription(), res);
            addObjectNode("modDate",
                    formatXMLDate(embeddedFileFeaturesData.getModDate()), res);
            addObjectNode("name", embeddedFileFeaturesData.getName(), res);
            addObjectNode("size", embeddedFileFeaturesData.getSize(), res);
            addObjectNode("subtype", embeddedFileFeaturesData.getSubtype(), res);

        } catch (FeatureParsingException | DatatypeConfigurationException e) {
            LOGGER.error(e);
        }
        return res;
    }

    private static void addObjectNode(String nodeName, Object toAdd,
            List<FeatureTreeNode> list) throws FeatureParsingException {
        if (toAdd != null) {
            FeatureTreeNode node = FeatureTreeNode.createRootNode(nodeName);
            node.setValue(toAdd.toString());
            list.add(node);
        }
    }

    private static String formatXMLDate(Calendar calendar)
            throws DatatypeConfigurationException {
        if (calendar == null) {
            return null;
        }
        GregorianCalendar greg = new GregorianCalendar(Locale.US);
        greg.setTime(calendar.getTime());
        greg.setTimeZone(calendar.getTimeZone());
        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(greg);
        return xmlCalendar.toXMLFormat();

    }

}
