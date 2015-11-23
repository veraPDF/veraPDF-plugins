package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractFontFeaturesExtractor;
import org.verapdf.features.FontFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class FontSampleExtractor extends AbstractFontFeaturesExtractor {

	private static final Logger LOGGER = Logger
			.getLogger(FontSampleExtractor.class);

	@Override
	public List<FeatureTreeNode> getFontFeatures(FontFeaturesData fontFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
			FeatureTreeNode stream = FeatureTreeNode.createRootNode("streamContent");
			stream.setValue(DatatypeConverter.printHexBinary(fontFeaturesData.getStream()));
			res.add(stream);

			byte[] meta = fontFeaturesData.getMetadata();
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
		return "8b06613d-b5d0-47b5-a7e6-4900cea4823c";
	}

	@Override
	public String getDescription() {
		return "Sample font extractor.";
	}
}
