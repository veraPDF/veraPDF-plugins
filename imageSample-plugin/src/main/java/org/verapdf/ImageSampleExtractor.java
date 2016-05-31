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
    public static final String ID = "163e5726-3c5a-4701-abdc-428005c8c39d";
    public static final String DESCRIPTION = "This sample Extractor generates custom features report containing data from incoming " +
                "ImageFeaturesData object.";

    private static final Logger LOGGER = Logger
            .getLogger(ImageSampleExtractor.class);

    public ImageSampleExtractor() {
        super(ID, DESCRIPTION);
    }

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
				metadata.setValue(DatatypeConverter.printHexBinary(meta));
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

					//Special case for JBIG2Decode filter
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
		FeatureTreeNode node = null;
		if (toAdd != null) {
			node = FeatureTreeNode.createRootNode(nodeName);
			list.add(node);
			node.setValue(toAdd.toString());
		}
		return node;
	}
}
