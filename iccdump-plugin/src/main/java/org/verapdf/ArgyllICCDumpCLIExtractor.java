package org.verapdf;

import org.apache.log4j.Logger;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractICCProfileFeaturesExtractor;
import org.verapdf.features.ICCProfileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class ArgyllICCDumpCLIExtractor extends AbstractICCProfileFeaturesExtractor {

	private static final Logger LOGGER = Logger
			.getLogger(ArgyllICCDumpCLIExtractor.class);

	private String FILE_PATH;

	@Override
	public List<FeatureTreeNode> getICCProfileFeatures(ICCProfileFeaturesData iccProfileFeaturesData) {
		if (this.FILE_PATH == null) {
			this.FILE_PATH = getFolderPath().toString() + "/iccProfile.icc";
		}
		List<FeatureTreeNode> res = null;
		generateICCProfile(iccProfileFeaturesData);
		try {
			res = execCLI();
		} catch (InterruptedException | FeatureParsingException e) {
			LOGGER.error(e);
		}
		try {
			clean();
		} catch (IOException e) {
			LOGGER.error(e);
		}

		return res;
	}

	private List<FeatureTreeNode> execCLI() throws InterruptedException, FeatureParsingException {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
			Runtime rt = Runtime.getRuntime();
			String filePath = new File(FILE_PATH).getAbsolutePath();
			String[] str = new String[]{getFolderPath().toString() + "/iccdump", "-v", "1", filePath};
			Process pr = rt.exec(str);
			BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String line = reader.readLine();
			while (line != null) {
				if ("Header:".equals(line.trim())) {
					FeatureTreeNode header = FeatureTreeNode.createRootNode("header");
					line = reader.readLine();

					while (line != null && !("".equals(line.trim()))) {
						int eqind = line.indexOf("=");
						String name = line.substring(0, eqind).trim();
						String value = line.substring(eqind + 1).trim();
						FeatureTreeNode entry = FeatureTreeNode.createChildNode("headerEntry", header);
						entry.setAttribute("name", name);
						entry.setAttribute("value", value);
						line = reader.readLine();
					}
					res.add(header);
				} else if (line.trim().startsWith("tag")) {
					FeatureTreeNode tag = FeatureTreeNode.createRootNode("tag");
					tag.setAttribute("number", line.trim().substring(4, line.length() - 1));

					line = reader.readLine();
					while (line != null && !("".equals(line.trim()))) {
						int spind = line.trim().indexOf(" ");
						String name = line.trim().substring(0, spind);
						String value = line.trim().substring(spind).trim();
						FeatureTreeNode.createChildNode(name, tag).setValue(value);
						line = reader.readLine();
					}
					res.add(tag);
				}

				line = reader.readLine();
			}
			pr.waitFor();
		} catch (IOException e) {
			LOGGER.error(e);
		}

		if (res.isEmpty()) {
			FeatureTreeNode error = FeatureTreeNode.createRootNode("error");
			error.setValue("Failed to find any header or tag information." +
					" Probably there is no iccdump executable in the plugin folder or the executable is not compatible " +
					"with your Operating System.");
			res.add(error);
		}
		return res;
	}

	private void generateICCProfile(ICCProfileFeaturesData featuresData) {
		byte[] icc = featuresData.getStream();
		try {
			FileOutputStream out = new FileOutputStream(FILE_PATH);
			out.write(icc);
			out.close();
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	private void clean() throws IOException {
		Files.deleteIfExists(new File(FILE_PATH).toPath());
	}

	@Override
	public String getID() {
		return "640a9bd1-219f-42db-8d9d-7bd67de48fce";
	}

	@Override
	public String getDescription() {
		return "This Extractor generates custom features report containing header and information about tags from " +
				"the iccProfile using the Argyll iccdump command line application.";
	}
}
