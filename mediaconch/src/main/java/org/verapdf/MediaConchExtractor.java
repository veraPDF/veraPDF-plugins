package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;

/**
 * @author Maksim Bezrukov
 */
public class MediaConchExtractor extends AbstractEmbeddedFileFeaturesExtractor {
    private static final String ID = "8725b233-1597-490e-9b45-b989303d2c5b";
    private static final String DESCRIPTION = "Generates mediaconch report of the given embedded video file";

    public MediaConchExtractor() {
        super(ID, DESCRIPTION);
    }

    @Override
    public List<FeatureTreeNode> getEmbeddedFileFeatures(EmbeddedFileFeaturesData embeddedFileFeaturesData) {
        if (!isValidType(embeddedFileFeaturesData.getSubtype())) {
            return null;
        }
        List<FeatureTreeNode> result = new ArrayList<>();
        try {
            try {
                MediaConchConfig config = getConfig(result);
                File temp = generateTempFile(embeddedFileFeaturesData.getStream(), embeddedFileFeaturesData.getName());
                execCLI(result, config, temp);
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

    private void execCLI(List<FeatureTreeNode> nodes, MediaConchConfig config, File temp) throws InterruptedException, FeatureParsingException, IOException {
        Runtime rt = Runtime.getRuntime();
        String cliPath;
        String configCliPath = config.getCliPath();
        if (configCliPath == null || configCliPath.isEmpty()) {
            cliPath = "mediaconch";
        } else {
            cliPath = configCliPath;
        }
        File out = getOutFile(config, nodes);
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

    private File getOutFile(MediaConchConfig config, List<FeatureTreeNode> nodes) throws FeatureParsingException, IOException {
        if (config.getOutFolder() == null) {
            return getOutFileInFolder(getTempFolder());
        } else {
            File outFolder = new File(config.getOutFolder());
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

    private MediaConchConfig getConfig(List<FeatureTreeNode> nodes) throws FeatureParsingException {
        MediaConchConfig config = MediaConchConfig.defaultInstance();
        File conf = getConfigFile();
        if (conf.isFile() && conf.canRead()) {
            try {
                config = MediaConchConfig.fromXml(new FileInputStream(conf));
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

    private boolean isValidType(String type) {
        return type.toLowerCase().startsWith("video/");
    }

}

