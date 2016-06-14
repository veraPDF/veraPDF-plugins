package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractFontFeaturesExtractor;
import org.verapdf.features.FontFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class OTSExtractor extends AbstractFontFeaturesExtractor {
	public static final String ID = "f04e45de-78ff-48e6-8ff9-a0c39ef9e4f9";
	public static final String DESCRIPTION = "Validates font using OT-Sanitise";

	public OTSExtractor() {
		super(ID, DESCRIPTION);
	}

	@Override
	public List<FeatureTreeNode> getFontFeatures(FontFeaturesData fontFeaturesData) {
		List<FeatureTreeNode> result = new ArrayList<>();
		try {
			try {
				OTSConfig config = getConfig(result);
				File temp = generateTempFile(fontFeaturesData.getStream(), "fnt");
				exec(result, config, temp);
			} catch (IOException | InterruptedException | URISyntaxException e) {
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

	private void exec(List<FeatureTreeNode> nodes, OTSConfig config, File temp) throws InterruptedException, FeatureParsingException, IOException, URISyntaxException {
		String exePath = getExecutablePath(config);
		if (exePath == null) {
			FeatureTreeNode error = FeatureTreeNode.createRootNode("error");
			error.setValue("Can not obtain ot-sanitise binary");
			nodes.add(error);
			return;
		}
		String[] args = new String[]{exePath, temp.getAbsolutePath()};

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(args);
		BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

		StringBuilder builder = new StringBuilder("");
		String line = reader.readLine();
		while (line != null) {
			builder.append(line + "\n");
			line = reader.readLine();
		}
		pr.waitFor();

		String output = builder.toString();
		boolean isValid = output.isEmpty();
		FeatureTreeNode valid = FeatureTreeNode.createRootNode("isValid");
		valid.setValue(Boolean.toString(isValid));
		nodes.add(valid);

		if (!isValid) {
			FeatureTreeNode res = FeatureTreeNode.createRootNode("otsOutput");
			res.setValue(output.substring(0, output.length()-1));
			nodes.add(res);
		}
	}

	private static File getTempFolder() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File tempFolder = new File(tempDir, "veraPDFOTSPluginTemp");
		if (!tempFolder.exists()) {
			tempFolder.mkdir();
		}
		tempFolder.deleteOnExit();
		return tempFolder;
	}

	private OTSConfig getConfig(List<FeatureTreeNode> nodes) throws FeatureParsingException {
		OTSConfig config = OTSConfig.defaultInstance();
		File conf = getConfigFile();
		if (conf.isFile() && conf.canRead()) {
			try {
				config = OTSConfig.fromXml(new FileInputStream(conf));
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

	private String getExecutablePath(OTSConfig config) {
		String cliPath = config.getCliPath();
		if (cliPath == null) {
			cliPath = getFolderPath().toString() + "/ots/ot-sanitise";
		}

		File cli = new File(cliPath);
		if (!(cli.exists() && cli.isFile())) {
			return null;
		}
		return cliPath;
	}
}
