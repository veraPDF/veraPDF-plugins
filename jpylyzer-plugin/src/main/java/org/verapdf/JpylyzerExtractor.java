package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractImageFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.ImageFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.ArrayList;
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
		List<FeatureTreeNode> result = new ArrayList<>();
		try {
			try {
				JpylyzerConfig config = getConfig(result);
				File temp = generateTempFile(imageFeaturesData.getStream(), "jpx");
				exec(result, config, temp);
			} catch (IOException | InterruptedException e) {
				FeatureTreeNode node = FeatureTreeNode.createRootNode("error");
				node.setValue("Error in execution. Error message: " + e.getMessage());
				result.add(node);
			}
		} catch (FeatureParsingException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

	private File generateTempFile(byte[] stream, String name) throws IOException {
		File fold = getTempFolder();
		File temp = File.createTempFile(name == null ? "" : name, "", fold);
		temp.deleteOnExit();
		FileOutputStream out = new FileOutputStream(temp);
		out.write(stream);
		out.close();
		return temp;
	}

	private void exec(List<FeatureTreeNode> nodes, JpylyzerConfig config, File temp) throws InterruptedException, FeatureParsingException, IOException {


//		FeatureTreeNode node = FeatureTreeNode.createRootNode("resultPath");
//		node.setValue(out.getCanonicalPath());
//		nodes.add(node);
	}

	private File getOutFile(JpylyzerConfig config, List<FeatureTreeNode> nodes) throws FeatureParsingException, IOException {
		if (config.getOutFolder() == null) {
			File tempFolder = getTempFolder();
			return getOutFileInFolder(tempFolder);
		} else {
			File outFolder = new File(config.getOutFolder());
			if (outFolder.isDirectory()) {
				return getOutFileInFolder(outFolder);
			} else {
				FeatureTreeNode node = FeatureTreeNode.createRootNode("error");
				node.setValue("Config file contains out folder path but it doesn't link a directory.");
				nodes.add(node);
				File tempFolder = getTempFolder();
				return getOutFileInFolder(tempFolder);
			}
		}
	}

	private File getTempFolder() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File tempFolder = new File(tempDir, "veraPDFJpylyzerPluginTemp");
		if (!tempFolder.exists()) {
			tempFolder.mkdir();
		}
		return tempFolder;
	}

	private File getOutFileInFolder(File folder) throws IOException {
		File out = File.createTempFile("veraPDF_Jpylyzer_Plugin_out", ".xml", folder);
		return out;
	}

	private JpylyzerConfig getConfig(List<FeatureTreeNode> nodes) throws FeatureParsingException {
		JpylyzerConfig config = JpylyzerConfig.defaultInstance();
		File conf = getConfigFile();
		if (conf.isFile() && conf.canRead()) {
			try {
				config = JpylyzerConfig.fromXml(new FileInputStream(conf));
			} catch (JAXBException | FileNotFoundException e) {
				FeatureTreeNode node = FeatureTreeNode.createRootNode("error");
				node.setValue("Config file contains wrong syntax. Error message: " + e.getMessage());
				nodes.add(node);
			}
		}
		return config;
	}

	private File getConfigFile() {
		return new File(getFolderPath().toFile(), "config.xml");
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
