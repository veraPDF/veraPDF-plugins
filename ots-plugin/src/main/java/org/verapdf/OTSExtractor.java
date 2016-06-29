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

	@Override
	public List<FeatureTreeNode> getFontFeatures(FontFeaturesData fontFeaturesData) {
		List<FeatureTreeNode> result = new ArrayList<>();
		try {
			try {
				File temp = generateTempFile(fontFeaturesData.getStream(), "fnt");
				exec(result, temp);
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

	private void exec(List<FeatureTreeNode> nodes, File temp) throws InterruptedException, FeatureParsingException, IOException, URISyntaxException {
		String exePath = getAttributes().get("cliPath");
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
}
