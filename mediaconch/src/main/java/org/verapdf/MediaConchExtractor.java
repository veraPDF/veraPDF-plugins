/**
 * This file is part of mediaconch-plugin, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * mediaconch-plugin is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with mediaconch-plugin as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * mediaconch-plugin as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import java.io.*;
import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class MediaConchExtractor extends AbstractEmbeddedFileFeaturesExtractor {

	@Override
	public List<FeatureTreeNode> getEmbeddedFileFeatures(EmbeddedFileFeaturesData embeddedFileFeaturesData) {
		if (!isValidType(embeddedFileFeaturesData.getSubtype())) {
			return null;
		}
		List<FeatureTreeNode> result = new ArrayList<>();
		try {
			try {
				File temp = generateTempFile(embeddedFileFeaturesData.getStream(), embeddedFileFeaturesData.getName());
				execCLI(result, temp);
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

	private void execCLI(List<FeatureTreeNode> nodes, File temp) throws InterruptedException, FeatureParsingException, IOException {
		Runtime rt = Runtime.getRuntime();
		String cliPath;
		String configCliPath = getAttributes().get("cliPath");
		if (configCliPath == null || configCliPath.isEmpty()) {
			cliPath = "mediaconch";
		} else {
			cliPath = configCliPath;
		}
		File out = getOutFile(getAttributes().get("outFolder"), nodes);
		String[] str = new String[]{cliPath, "-mc", "-fx", temp.getCanonicalPath()};
		Process pr = rt.exec(str);
		FileOutputStream outStream = new FileOutputStream(out);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = pr.getInputStream().read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		pr.waitFor();
		FeatureTreeNode node = FeatureTreeNode.createRootNode("resultPath");
		node.setValue(out.getCanonicalPath());
		nodes.add(node);

	}

	private File getOutFile(String outFolderPath, List<FeatureTreeNode> nodes) throws FeatureParsingException, IOException {
		if (outFolderPath == null) {
			return getOutFileInFolder(getTempFolder());
		} else {
			File outFolder = new File(outFolderPath);
			if (outFolder.isDirectory()) {
				return getOutFileInFolder(outFolder);
			} else {
				FeatureTreeNode node = FeatureTreeNode.createRootNode("error");
				node.setValue("Config file contains out folder path but it doesn't link a directory.");
				nodes.add(node);
				return getOutFileInFolder(getTempFolder());
			}
		}
	}

	private File getTempFolder() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File tempFolder = new File(tempDir, "veraPDFMediaConchPluginTemp");
		if (!tempFolder.exists()) {
			tempFolder.mkdir();
		}
		return tempFolder;
	}

	private File getOutFileInFolder(File folder) throws IOException {
		return File.createTempFile("veraPDF_MediaConch_Plugin_out", ".xml", folder);
	}

	private boolean isValidType(String type) {
		return type.toLowerCase().startsWith("video/");
	}

}

