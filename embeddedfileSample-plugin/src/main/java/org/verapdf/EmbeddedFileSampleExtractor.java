package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import jakarta.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class EmbeddedFileSampleExtractor extends
        AbstractEmbeddedFileFeaturesExtractor {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedFileSampleExtractor.class.getCanonicalName());

    @Override
    public List<FeatureTreeNode> getEmbeddedFileFeatures(
            EmbeddedFileFeaturesData embeddedFileFeaturesData) {
        List<FeatureTreeNode> res = new ArrayList<>();
        try {
//            FeatureTreeNode stream = FeatureTreeNode
//                    .createRootNode("streamContent");
//            stream.setValue(DatatypeConverter
//                    .printHexBinary(inputStreamToByteArray(embeddedFileFeaturesData.getStream())));
//            res.add(stream);

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

        } catch (/*IOException |*/ FeatureParsingException | DatatypeConfigurationException e) {
			LOGGER.log(Level.WARNING, "IO/Exception when adding information", e);
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

//    private static byte[] inputStreamToByteArray(InputStream is) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        byte[] bytes = new byte[1024];
//        int length;
//        while ((length = is.read(bytes)) != -1) {
//            baos.write(bytes, 0, length);
//        }
//        return baos.toByteArray();
//    }
}
