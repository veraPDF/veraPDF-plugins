/**
 *
 */
package org.verapdf;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;

/**
 * @author Maksim Bezrukov
 */
@XmlRootElement(namespace = "http://www.verapdf.org/MediaConchConfig", name = "mediaconchConfig")
final class MediaConchConfig {

    @XmlElement
    private final String cliPath;
    @XmlElement
    private final String outFolder;

    private MediaConchConfig() {
        this("", "");
    }

    private MediaConchConfig(String outFolder, String cliPath) {
        this.outFolder = outFolder;
        this.cliPath = cliPath;
    }

    public String getCliPath() {
        return cliPath;
    }

    public String getOutFolder() {
        return outFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaConchConfig that = (MediaConchConfig) o;

        if (cliPath != null ? !cliPath.equals(that.cliPath) : that.cliPath != null) return false;
        return !(outFolder != null ? !outFolder.equals(that.outFolder) : that.outFolder != null);

    }

    @Override
    public int hashCode() {
        int result = cliPath != null ? cliPath.hashCode() : 0;
        result = 31 * result + (outFolder != null ? outFolder.hashCode() : 0);
        return result;
    }

    static MediaConchConfig defaultInstance() {
        return new MediaConchConfig("mediaconch", null);
    }

    static MediaConchConfig fromValues(final String cliPath, final String outFolder) {
        return new MediaConchConfig(cliPath, outFolder);
    }

    static String toXml(final MediaConchConfig toConvert, Boolean prettyXml)
            throws JAXBException, IOException {
        String retVal = "";
        try (StringWriter writer = new StringWriter()) {
            toXml(toConvert, writer, prettyXml);
            retVal = writer.toString();
            return retVal;
        }
    }

    static MediaConchConfig fromXml(final String toConvert)
            throws JAXBException {
        try (StringReader reader = new StringReader(toConvert)) {
            return fromXml(reader);
        }
    }

    static void toXml(final MediaConchConfig toConvert,
                      final OutputStream stream, Boolean prettyXml) throws JAXBException {
        Marshaller varMarshaller = getMarshaller(prettyXml);
        varMarshaller.marshal(toConvert, stream);
    }

    static MediaConchConfig fromXml(final InputStream toConvert)
            throws JAXBException {
        Unmarshaller stringUnmarshaller = getUnmarshaller();
        return (MediaConchConfig) stringUnmarshaller.unmarshal(toConvert);
    }

    static void toXml(final MediaConchConfig toConvert, final Writer writer,
                      Boolean prettyXml) throws JAXBException {
        Marshaller varMarshaller = getMarshaller(prettyXml);
        varMarshaller.marshal(toConvert, writer);
    }

    static MediaConchConfig fromXml(final Reader toConvert)
            throws JAXBException {
        Unmarshaller stringUnmarshaller = getUnmarshaller();
        return (MediaConchConfig) stringUnmarshaller.unmarshal(toConvert);
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        JAXBContext context = JAXBContext
                .newInstance(MediaConchConfig.class);
        return context.createUnmarshaller();
    }

    private static Marshaller getMarshaller(Boolean setPretty)
            throws JAXBException {
        JAXBContext context = JAXBContext
                .newInstance(MediaConchConfig.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, setPretty);
        return marshaller;
    }
}
