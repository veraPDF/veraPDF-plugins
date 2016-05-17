package org.verapdf;

import org.verapdf.features.AbstractImageFeaturesExtractor;
import org.verapdf.features.ImageFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class JpylyzerExtractor extends AbstractImageFeaturesExtractor {

	@Override
	public List<FeatureTreeNode> getImageFeatures(ImageFeaturesData imageFeaturesData) {
		boolean doesContainsJPXFilter = false;
		for (ImageFeaturesData.Filter filter : imageFeaturesData.getFilters()) {
			if ("JPXDecode".equals(filter.getName())) {
				doesContainsJPXFilter = true;
				break;
			}
		}
		if (!doesContainsJPXFilter) {
			return null;
		}


	}

	@Override
	public String getID() {
		return "3ee4e6b3-af6b-4510-8b95-1af29fc81629";
	}

	@Override
	public String getDescription() {
		return "Extracts features of the Image using Jpylyzer";
	}
}
