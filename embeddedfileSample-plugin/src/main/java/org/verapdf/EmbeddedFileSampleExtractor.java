/**
 * This file is part of embeddedfileSample-plugin, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * embeddedfileSample-plugin is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with embeddedfileSample-plugin as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * embeddedfileSample-plugin as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
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
