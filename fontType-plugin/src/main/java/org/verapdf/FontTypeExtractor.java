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

	private static final byte[] OPENTYPE_BEGIN = new byte[]{0x4f, 0x54, 0x54, 0x4f};
	private static final byte[] PS_TYPE1_BEGIN = new byte[]{0x25, 0x21};
	private static final byte[] TRUE_TYPE_BEGIN = new byte[]{0x00, 0x01, 0x00, 0x00};
	private static final byte[] TRUE_TYPE_TRUE_BEGIN = new byte[]{0x74, 0x72, 0x75, 0x65};

	private static final String UNDEFINED = "Undefined";

	@Override
	public List<FeatureTreeNode> getFontFeatures(FontFeaturesData fontFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();

		String fontType = getFontType(fontFeaturesData.getStream());
		try {
			FeatureTreeNode fontTypeFromFile = FeatureTreeNode.createRootNode("fontTypeFromFile");
			fontTypeFromFile.setValue(fontType);
			res.add(fontTypeFromFile);
		} catch (FeatureParsingException e) {
			LOGGER.error(e);
		}

		return res;
	}

	private static String getFontType(byte[] file) {
		if (startsWith(file, PS_TYPE1_BEGIN)) {
			return "PS Type1";
		} else if (startsWith(file, OPENTYPE_BEGIN)) {
			return "OpenType";
		} else if (startsWith(file, TRUE_TYPE_BEGIN) || startsWith(file, TRUE_TYPE_TRUE_BEGIN)) {
			return "TrueType";
		} else if (file[0] == 1 && (file[1] >= 0 && file[1] <= 5)) {
			return "CFF Type1";
		} else {
			return UNDEFINED;
		}
	}

	private static boolean startsWith(byte[] orig, byte[] pattern) {
		if (orig.length < pattern.length) {
			return false;
		}
		for (int i = 0; i < pattern.length; ++i) {
			if (orig[i] != pattern[i]) {
				return false;
			}
		}
		return true;
	}
}
