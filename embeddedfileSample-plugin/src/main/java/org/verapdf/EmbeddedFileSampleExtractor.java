package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class EmbeddedFileSampleExtractor extends AbstractEmbeddedFileFeaturesExtractor {

	private static final Logger LOGGER = Logger
			.getLogger(EmbeddedFileSampleExtractor.class);

	@Override
	public List<FeatureTreeNode> getEmbeddedFileFeatures(EmbeddedFileFeaturesData embeddedFileFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
			FeatureTreeNode stream = FeatureTreeNode.createRootNode("streamContent");
			stream.setValue(DatatypeConverter.printHexBinary(embeddedFileFeaturesData.getStream()));
			res.add(stream);


		} catch (FeatureParsingException e) {
			LOGGER.error("Some fail in logic", e);
		}
		return res;
	}

	@Override
	public String getID() {
		return "b99ad964-5d34-4b17-9bec-b19a174ae772";
	}

	@Override
	public String getDescription() {
		return "Sample embedded file extractor.";
	}
}
