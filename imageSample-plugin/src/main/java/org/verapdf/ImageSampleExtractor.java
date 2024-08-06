/**
 * This file is part of imageSample-plugin, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * imageSample-plugin is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with imageSample-plugin as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * imageSample-plugin as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractImageFeaturesExtractor;
import org.verapdf.features.ImageFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class ImageSampleExtractor extends AbstractImageFeaturesExtractor {

    private static final Logger LOGGER = Logger.getLogger(ImageSampleExtractor.class.getCanonicalName());

	@Override
	public List<FeatureTreeNode> getImageFeatures(ImageFeaturesData imageFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
//			FeatureTreeNode stream = FeatureTreeNode.createRootNode("streamContent");
//			stream.setValue(DatatypeConverter.printHexBinary(inputStreamToByteArray(imageFeaturesData.getStream())));
//			res.add(stream);

			InputStream meta = imageFeaturesData.getMetadata();
			if (meta != null) {
				FeatureTreeNode metadata = FeatureTreeNode.createRootNode("metadataStreamContent");
				metadata.setValue(DatatypeConverter.printHexBinary(inputStreamToByteArray(meta)));
				res.add(metadata);
			}

			addObjectNode("width", imageFeaturesData.getWidth(), res);
			addObjectNode("height", imageFeaturesData.getHeight(), res);

			List<ImageFeaturesData.Filter> filters = imageFeaturesData.getFilters();
			if (filters != null) {
				FeatureTreeNode filtersNode = FeatureTreeNode.createRootNode("filters");
				res.add(filtersNode);
				for (ImageFeaturesData.Filter filter : filters) {
					FeatureTreeNode filterNode = filtersNode.addChild("filter");
					filterNode.setAttribute("name", String.valueOf(filter.getName()));
					Map<String, String> properties = filter.getProperties();
					if (properties != null) {
						for (Map.Entry entry : properties.entrySet()) {
							filterNode.addChild(String.valueOf(entry.getKey())).setValue(String.valueOf(entry.getValue()));
						}
					}

					//Special case for JBIG2Decode filter
//					InputStream streamF = filter.getStream();
//					if (streamF != null) {
//						String streamContent = DatatypeConverter.printHexBinary(inputStreamToByteArray(streamF));
//						filterNode.addChild("stream").setValue(streamContent);
//					}
				}
			}

		} catch (FeatureParsingException | IOException e) {
			LOGGER.log(Level.WARNING, "Some fail in logic", e);
		}
		return res;
	}

	private static FeatureTreeNode addObjectNode(String nodeName, Object toAdd, List<FeatureTreeNode> list) throws FeatureParsingException {
		FeatureTreeNode node = null;
		if (toAdd != null) {
			node = FeatureTreeNode.createRootNode(nodeName);
			list.add(node);
			node.setValue(toAdd.toString());
		}
		return node;
	}

	private static byte[] inputStreamToByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024];
		int length;
		while ((length = is.read(bytes)) != -1) {
			baos.write(bytes, 0, length);
		}
		return baos.toByteArray();
	}
}
