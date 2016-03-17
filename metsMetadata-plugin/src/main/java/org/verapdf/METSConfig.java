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
@XmlRootElement(namespace = "http://www.verapdf.org/METSConfig", name = "metsConfig")
final class METSConfig {
    
    @XmlElement
    private final String outFolder;

    private METSConfig() {
        this("");
    }

    private METSConfig(String outFolder) {
        this.outFolder = outFolder;
    }

    public String getOutFolder() {
        return outFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        METSConfig that = (METSConfig) o;

        return !(outFolder != null ? !outFolder.equals(that.outFolder) : that.outFolder != null);

    }

    @Override
    public int hashCode() {
        return outFolder != null ? outFolder.hashCode() : 0;
    }

    static METSConfig defaultInstance() {
        return new METSConfig(null);
    }

    static METSConfig fromValues(final String outFolder) {
        return new METSConfig(outFolder);
    }

    static String toXml(final METSConfig toConvert, Boolean prettyXml)
            throws JAXBException, IOException {
        String retVal = "";
        try (StringWriter writer = new StringWriter()) {
            toXml(toConvert, writer, prettyXml);
            retVal = writer.toString();
            return retVal;
        }
    }

    static METSConfig fromXml(final String toConvert)
            throws JAXBException {
        try (StringReader reader = new StringReader(toConvert)) {
            return fromXml(reader);
        }
    }

    static void toXml(final METSConfig toConvert,
                      final OutputStream stream, Boolean prettyXml) throws JAXBException {
        Marshaller varMarshaller = getMarshaller(prettyXml);
        varMarshaller.marshal(toConvert, stream);
    }

    static METSConfig fromXml(final InputStream toConvert)
            throws JAXBException {
        Unmarshaller stringUnmarshaller = getUnmarshaller();
        return (METSConfig) stringUnmarshaller.unmarshal(toConvert);
    }

    static void toXml(final METSConfig toConvert, final Writer writer,
                      Boolean prettyXml) throws JAXBException {
        Marshaller varMarshaller = getMarshaller(prettyXml);
        varMarshaller.marshal(toConvert, writer);
    }

    static METSConfig fromXml(final Reader toConvert)
            throws JAXBException {
        Unmarshaller stringUnmarshaller = getUnmarshaller();
        return (METSConfig) stringUnmarshaller.unmarshal(toConvert);
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        JAXBContext context = JAXBContext
                .newInstance(METSConfig.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return unmarshaller;
    }

    private static Marshaller getMarshaller(Boolean setPretty)
            throws JAXBException {
        JAXBContext context = JAXBContext
                .newInstance(METSConfig.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, setPretty);
        return marshaller;
    }
}
