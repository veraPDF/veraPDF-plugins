/**
 * This file is part of ots-plugin, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * ots-plugin is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with ots-plugin as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * ots-plugin as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractFontFeaturesExtractor;
import org.verapdf.features.FontFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

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

	private File generateTempFile(InputStream stream, String name) throws IOException {
		File fold = getTempFolder();
		File temp = File.createTempFile(name == null ? "" : name, "", fold);
		temp.deleteOnExit();
		FileOutputStream out = new FileOutputStream(temp);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = stream.read(bytes)) != -1) {
			out.write(bytes, 0, length);
		}
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
			res.setValue(output.substring(0, output.length() - 1));
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
