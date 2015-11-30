package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractICCProfileFeaturesExtractor;
import org.verapdf.features.ICCProfileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class ICCProfileSampleExtractor extends AbstractICCProfileFeaturesExtractor {

	private static final Logger LOGGER = Logger
			.getLogger(ICCProfileSampleExtractor.class);

	@Override
	public List<FeatureTreeNode> getICCProfileFeatures(ICCProfileFeaturesData iccProfileFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
			FeatureTreeNode stream = FeatureTreeNode.createRootNode("streamContent");
			stream.setValue(DatatypeConverter.printHexBinary(iccProfileFeaturesData.getStream()));
			res.add(stream);

			byte[] meta = iccProfileFeaturesData.getMetadata();
			if (meta != null) {
				FeatureTreeNode metadata = FeatureTreeNode.createRootNode("metadataStreamContent");
				metadata.setValue(DatatypeConverter.printHexBinary(meta));
				res.add(metadata);
			}

			addObjectNode("nValue", iccProfileFeaturesData.getN(), res);

			List<Double> range = iccProfileFeaturesData.getRange();
			if (range != null) {
				FeatureTreeNode rangeNode = FeatureTreeNode.createRootNode("range");
				res.add(rangeNode);
				for (int i = 0; i < range.size(); ++i) {
					Double obj = range.get(i);
					if (obj != null) {
						FeatureTreeNode entry = FeatureTreeNode.createChildNode("entry", rangeNode);
						entry.setValue(obj.toString());
						entry.setAttribute("index", String.valueOf(i));
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

	@Override
	public String getID() {
		return "ad7c3157-5865-4e8a-8f47-57f81580b612";
	}

	@Override
	public String getDescription() {
		return "This sample Extractor generates custom features report containing data from incoming " +
				"ICCProfileFeaturesData object.";
	}
}
