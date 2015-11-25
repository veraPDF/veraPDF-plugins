package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractImageFeaturesExtractor;
import org.verapdf.features.ImageFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Maksim Bezrukov
 */
public class ImageSampleExtractor extends AbstractImageFeaturesExtractor {

	private static final Logger LOGGER = Logger
			.getLogger(ImageSampleExtractor.class);

	@Override
	public List<FeatureTreeNode> getImageFeatures(ImageFeaturesData imageFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
			FeatureTreeNode stream = FeatureTreeNode.createRootNode("streamContent");
			stream.setValue(DatatypeConverter.printHexBinary(imageFeaturesData.getStream()));
			res.add(stream);

			byte[] meta = imageFeaturesData.getMetadata();
			if (meta != null) {
				FeatureTreeNode metadata = FeatureTreeNode.createRootNode("metadataStreamContent");
				String metaValue = DatatypeConverter.printHexBinary(meta);
				metadata.setValue(metaValue);
				res.add(metadata);
			}

			addObjectNode("width", imageFeaturesData.getWidth(), res);
			addObjectNode("height", imageFeaturesData.getHeight(), res);

			List<ImageFeaturesData.Filter> filters = imageFeaturesData.getFilters();
			if (filters != null) {
				FeatureTreeNode filtersNode = FeatureTreeNode.createRootNode("filters");
				res.add(filtersNode);
				for (ImageFeaturesData.Filter filter : filters) {
					FeatureTreeNode filterNode = FeatureTreeNode.createChildNode("filter", filtersNode);
					filterNode.setAttribute("name", String.valueOf(filter.getName()));
					Map<String, String> properties = filter.getProperties();
					if (properties != null) {
						for (Map.Entry entry : properties.entrySet()) {
							FeatureTreeNode.createChildNode(String.valueOf(entry.getKey()), filterNode).setValue(String.valueOf(entry.getValue()));
						}
					}
					byte[] streamF = filter.getStream();
					if (streamF != null) {
						String streamContent = DatatypeConverter.printHexBinary(streamF);
						FeatureTreeNode.createChildNode("stream", filterNode).setValue(streamContent);
					}
				}
			}

		} catch (FeatureParsingException e) {
			LOGGER.error("Some fail in logic", e);
		}
		return res;
	}

	private static FeatureTreeNode addObjectNode(String nodeName, Object toAdd, List<FeatureTreeNode> list) throws FeatureParsingException {
		FeatureTreeNode node = FeatureTreeNode.createRootNode(nodeName);
		list.add(node);
		if (toAdd != null) {
			node.setValue(toAdd.toString());
		}
		return node;
	}

	@Override
	public String getID() {
		return "163e5726-3c5a-4701-abdc-428005c8c39d";
	}

	@Override
	public String getDescription() {
		return "Sample image extractor.";
	}
}
