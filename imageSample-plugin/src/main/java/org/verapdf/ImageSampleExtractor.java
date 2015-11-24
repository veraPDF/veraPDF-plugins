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
			FeatureTreeNode metadata = FeatureTreeNode.createRootNode("metadataStreamContent");
			String metaValue = meta == null ? "null" : DatatypeConverter.printHexBinary(meta);
			metadata.setValue(metaValue);
			res.add(metadata);

			FeatureTreeNode widthNode = FeatureTreeNode.createRootNode("width");
			res.add(widthNode);
			widthNode.setValue(String.valueOf(imageFeaturesData.getWidth()));

			FeatureTreeNode heightNode = FeatureTreeNode.createRootNode("height");
			res.add(heightNode);
			heightNode.setValue(String.valueOf(imageFeaturesData.getHeight()));

			FeatureTreeNode filtersNode = FeatureTreeNode.createRootNode("filters");
			res.add(filtersNode);

			List<ImageFeaturesData.Filter> filters = imageFeaturesData.getFilters();
			if (filters == null) {
				filtersNode.setValue("null");
			} else if (filters.size() == 0) {
				filtersNode.setValue("Filters array is empty");
			} else {
				for (ImageFeaturesData.Filter filter : filters) {
					FeatureTreeNode filterNode = FeatureTreeNode.createChildNode("filter", filtersNode);
					filterNode.setAttribute("name", String.valueOf(filter.getName()));
					Map<String, String> properties = filter.getProperties();
					if (properties == null) {
						FeatureTreeNode.createChildNode("properties", filterNode).setValue("null");
					} else if (properties.size() == 0) {
						FeatureTreeNode.createChildNode("properties", filterNode).setValue("empty");
					} else {
						for (Map.Entry entry : properties.entrySet()) {
							FeatureTreeNode.createChildNode(String.valueOf(entry.getKey()), filterNode).setValue(String.valueOf(entry.getValue()));
						}
					}
					byte[] streamF = filter.getStream();
					String streamContent = streamF == null ? "null" : DatatypeConverter.printHexBinary(streamF);
					FeatureTreeNode.createChildNode("stream", filterNode).setValue(streamContent);
				}
			}

		} catch (FeatureParsingException e) {
			LOGGER.error("Some fail in logic", e);
		}
		return res;
	}

	@Override
	public String getID() {
		return "163e5726-3c5a-4701-abdc-428005c8c39d";
	}

	@Override
	public String getDescription() {
		return "Sample iccprofile extractor.";
	}
}
