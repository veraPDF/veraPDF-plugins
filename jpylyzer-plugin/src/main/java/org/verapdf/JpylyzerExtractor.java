/**
 * This file is part of jpylyzer-plugin, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * jpylyzer-plugin is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with jpylyzer-plugin as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * jpylyzer-plugin as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractImageFeaturesExtractor;
import org.verapdf.features.ImageFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URISyntaxException;
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
				File temp = generateTempFile(imageFeaturesData.getStream(), "jpx");
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
		String scriptPath = getAttributes().get("cliPath");
		if (scriptPath == null) {
			FeatureTreeNode error = FeatureTreeNode.createRootNode("error");
			error.setValue("Can not obtain jpylyzer script or binary");
			nodes.add(error);
			return;
		}
		String[] args;
		String isVerbose = getAttributes().get("isVerbose");
		if (isVerbose != null && Boolean.valueOf(isVerbose)) {
			args = new String[3];
			args[0] = scriptPath;
			args[1] = "--verbose";
			args[2] = temp.getCanonicalPath();
		} else {
			args = new String[2];
			args[0] = scriptPath;
			args[1] = temp.getCanonicalPath();
		}
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(args);
		File out = getOutFile(getAttributes().get("outFolder"), nodes);
		FileOutputStream outStream = new FileOutputStream(out);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = pr.getInputStream().read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		pr.waitFor();
		outStream.close();
		FeatureTreeNode node = FeatureTreeNode.createRootNode("resultPath");
		node.setValue(out.getCanonicalPath());
		nodes.add(node);

		try {
			String isValidJP2Value = getXMLNodeValue("//jpylyzer/file/isValid", out);
			FeatureTreeNode validationNode = FeatureTreeNode.createRootNode("isValidJP2");
			validationNode.setValue(isValidJP2Value);
			nodes.add(validationNode);
		} catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
			FeatureTreeNode error = FeatureTreeNode.createRootNode("error");
			error.setValue("Error in obtaining validation result. Error message: " + e.getMessage());
			nodes.add(error);
		}
	}

	private static String getXMLNodeValue(String xPath, File xml) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(xml);
		XPathExpression xp = XPathFactory.newInstance().newXPath().compile(xPath);
		return xp.evaluate(document);
	}

	private static File getOutFile(String outPath, List<FeatureTreeNode> nodes) throws FeatureParsingException, IOException {
		if (outPath == null) {
			File tempFolder = getTempFolder();
			return getOutFileInFolder(tempFolder);
		} else {
			File outFolder = new File(outPath);
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

	private static File getTempFolder() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File tempFolder = new File(tempDir, "veraPDFJpylyzerPluginTemp");
		if (!tempFolder.exists()) {
			tempFolder.mkdir();
		}
		tempFolder.deleteOnExit();
		return tempFolder;
	}

	private static File getOutFileInFolder(File folder) throws IOException {
		File out = File.createTempFile("veraPDF_Jpylyzer_Plugin_out", ".xml", folder);
		out.deleteOnExit();
		return out;
	}
}
