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
			if (meta != null) {
				FeatureTreeNode metadata = FeatureTreeNode.createRootNode("metadataStreamContent");
				metadata.setValue(DatatypeConverter.printHexBinary(meta));
				res.add(metadata);
			}

			addObjectNode("ascent", fontFeaturesData.getAscent(), res);
			addObjectNode("avgWidth", fontFeaturesData.getAvgWidth(), res);
			addObjectNode("capHeight", fontFeaturesData.getCapHeight(), res);
			addObjectNode("charSet", fontFeaturesData.getCharSet(), res);
			addObjectNode("descent", fontFeaturesData.getDescent(), res);
			addObjectNode("flags", fontFeaturesData.getFlags(), res);

			List<Double> bbox = fontFeaturesData.getFontBBox();
			if (bbox != null) {
				FeatureTreeNode rangeNode = FeatureTreeNode.createRootNode("fontBBox");
				res.add(rangeNode);
				for (int i = 0; i < bbox.size(); ++i) {
					Double obj = bbox.get(i);
					if (obj != null) {
						FeatureTreeNode entry = FeatureTreeNode.createChildNode("entry", rangeNode);
						entry.setValue(obj.toString());
						entry.setAttribute("index", String.valueOf(i));
					}
				}
			}

			addObjectNode("fontFamily", fontFeaturesData.getFontFamily(), res);
			addObjectNode("fontName", fontFeaturesData.getFontName(), res);
			addObjectNode("fontStretch", fontFeaturesData.getFontStretch(), res);
			addObjectNode("fontWeight", fontFeaturesData.getFontWeight(), res);
			addObjectNode("italicAngle", fontFeaturesData.getItalicAngle(), res);
			addObjectNode("leading", fontFeaturesData.getLeading(), res);
			addObjectNode("maxWidth", fontFeaturesData.getMaxWidth(), res);
			addObjectNode("missingWidth", fontFeaturesData.getMissingWidth(), res);
			addObjectNode("stemH", fontFeaturesData.getStemH(), res);
			addObjectNode("stemV", fontFeaturesData.getStemV(), res);
			addObjectNode("xHeight", fontFeaturesData.getXHeight(), res);

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
