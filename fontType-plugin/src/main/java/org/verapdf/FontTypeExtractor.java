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

	private static final String FAIL = "Can not obtain font file type";

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
		if (isMatchs(file, PS_TYPE1_BEGIN)) {
			return "PS Type1";
		} else if (isMatchs(file, OPENTYPE_BEGIN)) {
			return "OpenType";
		} else if (isMatchs(file, TRUE_TYPE_BEGIN)) {
			return "TrueType";
		} else if (file[0] == 1 && (file[1] >= 0 && file[1] <= 5)) {
			return "CFF Type1";
		} else {
			return FAIL;
		}
	}

	private static boolean isMatchs(byte[] orig, byte[] match) {
		if (orig.length < match.length) {
			return false;
		}
		for (int i = 0; i < match.length; ++i) {
			if (orig[i] != match[i]) {
				return false;
			}
		}
		return true;
	}

	public String getDescription() {
		return "Extracts font type from the font file.";
	}

	public String getID() {
		return "f1a805ae-62ae-4520-9d3b-489e5ff4af68";
	}
}
