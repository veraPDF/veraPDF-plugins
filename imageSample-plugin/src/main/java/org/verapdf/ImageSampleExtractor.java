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
