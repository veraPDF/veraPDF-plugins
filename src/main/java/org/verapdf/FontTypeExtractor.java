package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractFontFeaturesExtractor;
import org.verapdf.features.FontFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class FontTypeExtractor extends AbstractFontFeaturesExtractor {

	private static final Logger LOGGER = Logger
			.getLogger(FontTypeExtractor.class);

	@Override
	public List<FeatureTreeNode> getFontFeatures(FontFeaturesData fontFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();

		String fontType = getFontType(fontFeaturesData.getStream());
		try {
			res.add(FeatureTreeNode.newRootInstanceWithValue("fontTypeFromFile", fontType));
		} catch (FeatureParsingException e) {
			LOGGER.error(e);
		}

		return res;
	}

	private static String getFontType(byte[] file) {
		//TODO: getFont file type
		return "Some font type";
	}

	public String getDescription() {
		return "Extracts font type from the font file.";
	}

	public String getID() {
		return "f1a805ae-62ae-4520-9d3b-489e5ff4af68";
	}
}
