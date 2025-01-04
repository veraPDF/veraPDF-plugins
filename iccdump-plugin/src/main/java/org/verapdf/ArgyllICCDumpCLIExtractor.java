/**
 * This file is part of iccdump-plugin, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * iccdump-plugin is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with iccdump-plugin as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * iccdump-plugin as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractICCProfileFeaturesExtractor;
import org.verapdf.features.ICCProfileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class ArgyllICCDumpCLIExtractor extends AbstractICCProfileFeaturesExtractor {

    private static final Logger LOGGER = Logger.getLogger(ArgyllICCDumpCLIExtractor.class.getCanonicalName());

	private File temp;

	@Override
	public List<FeatureTreeNode> getICCProfileFeatures(ICCProfileFeaturesData iccProfileFeaturesData) {
		try {
			temp = File.createTempFile("tempICC", ".icc");
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "IO Exception when creating temp file", e);
			return null;
		}

		List<FeatureTreeNode> res = null;
		generateICCProfile(iccProfileFeaturesData.getStream());
		try {
			res = execCLI();
		} catch (InterruptedException | FeatureParsingException e) {
			LOGGER.log(Level.WARNING, "Problem Executing the Argyll ICC Extractor", e);
		}
		try {
			clean();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "IO Exception during cleanup", e);
		}

		return res;
	}

	private List<FeatureTreeNode> execCLI() throws InterruptedException, FeatureParsingException {
		List<FeatureTreeNode> res = new ArrayList<>();
		try {
			Runtime rt = Runtime.getRuntime();
			String cliPath = getAttributes().get("cliPath");
			if (cliPath == null) {
				FeatureTreeNode error = FeatureTreeNode.createRootNode("error");
				error.setValue("Can not obtain iccdump path");
				res.add(error);
				return res;
			}
			String[] str = new String[]{cliPath, "-v", "1", temp.getAbsolutePath()};
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
						FeatureTreeNode entry = header.addChild("headerEntry");
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
						tag.addChild(name).setValue(value);
						line = reader.readLine();
					}
					res.add(tag);
				}

				line = reader.readLine();
			}
			pr.waitFor();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "IO/Exception when reading object stream", e);
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

	private void generateICCProfile(InputStream icc) {
		try (FileOutputStream out = new FileOutputStream(this.temp)){
			byte[] bytes = new byte[1024];
			int length;
			while ((length = icc.read(bytes)) != -1) {
				out.write(bytes, 0, length);
			}
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "IO/Exception when outputing icc to temp file", e);
		}
	}

	private void clean() throws IOException {
		Files.deleteIfExists(temp.toPath());
	}
}
