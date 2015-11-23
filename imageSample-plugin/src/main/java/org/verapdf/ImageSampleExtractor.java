package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractImageFeaturesExtractor;
import org.verapdf.features.ImageFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;

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
