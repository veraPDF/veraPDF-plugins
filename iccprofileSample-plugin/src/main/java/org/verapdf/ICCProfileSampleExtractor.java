package org.verapdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractICCProfileFeaturesExtractor;
import org.verapdf.features.ICCProfileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

/**
 * @author Maksim Bezrukov
 */
public class ICCProfileSampleExtractor extends AbstractICCProfileFeaturesExtractor {

    private static final Logger LOGGER = Logger.getLogger(ICCProfileSampleExtractor.class.getCanonicalName());

	@Override
	public List<FeatureTreeNode> getICCProfileFeatures(ICCProfileFeaturesData iccProfileFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
//			FeatureTreeNode stream = FeatureTreeNode.createRootNode("streamContent");
//			stream.setValue(DatatypeConverter.printHexBinary(inputStreamToByteArray(iccProfileFeaturesData.getStream())));
//			res.add(stream);

			InputStream meta = iccProfileFeaturesData.getMetadata();
			if (meta != null) {
				FeatureTreeNode metadata = FeatureTreeNode.createRootNode("metadataStreamContent");
				metadata.setValue(DatatypeConverter.printHexBinary(inputStreamToByteArray(meta)));
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
						FeatureTreeNode entry = rangeNode.addChild("entry");
						entry.setValue(obj.toString());
						entry.setAttribute("index", String.valueOf(i));
					}
				}
			}

		} catch (FeatureParsingException | IOException e) {
			LOGGER.log(Level.WARNING, "Some failure in ICC scampler", e);
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

	private static byte[] inputStreamToByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024];
		int length;
		while ((length = is.read(bytes)) != -1) {
			baos.write(bytes, 0, length);
		}
		return baos.toByteArray();
	}
}
