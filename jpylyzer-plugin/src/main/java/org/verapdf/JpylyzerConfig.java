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
@XmlRootElement(namespace = "http://www.verapdf.org/JpylyzerConfig", name = "jpylyzerConfig")
final class JpylyzerConfig {

	@XmlElement
	private final String outFolder;
	@XmlElement
	private final boolean isVerbose;

	private JpylyzerConfig() {
		this("", false);
	}

	private JpylyzerConfig(String outFolder, boolean isVerbose) {
		this.outFolder = outFolder;
		this.isVerbose = isVerbose;
	}

	public String getOutFolder() {
		return outFolder;
	}

	public boolean isVerbose() {
		return isVerbose;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JpylyzerConfig that = (JpylyzerConfig) o;

		if (isVerbose != that.isVerbose) return false;
		return outFolder != null ? outFolder.equals(that.outFolder) : that.outFolder == null;

	}

	@Override
	public int hashCode() {
		int result = outFolder != null ? outFolder.hashCode() : 0;
		result = 31 * result + (isVerbose ? 1 : 0);
		return result;
	}

	static JpylyzerConfig defaultInstance() {
		return new JpylyzerConfig(null, false);
	}

	static JpylyzerConfig fromValues(final String outFolder, final boolean isVerbose) {
		return new JpylyzerConfig(outFolder, isVerbose);
	}

	static String toXml(final JpylyzerConfig toConvert, Boolean prettyXml)
			throws JAXBException, IOException {
		String retVal = "";
		try (StringWriter writer = new StringWriter()) {
			toXml(toConvert, writer, prettyXml);
			retVal = writer.toString();
			return retVal;
		}
	}

	static JpylyzerConfig fromXml(final String toConvert)
			throws JAXBException {
		try (StringReader reader = new StringReader(toConvert)) {
			return fromXml(reader);
		}
	}

	static void toXml(final JpylyzerConfig toConvert,
					  final OutputStream stream, Boolean prettyXml) throws JAXBException {
		Marshaller varMarshaller = getMarshaller(prettyXml);
		varMarshaller.marshal(toConvert, stream);
	}

	static JpylyzerConfig fromXml(final InputStream toConvert)
			throws JAXBException {
		Unmarshaller stringUnmarshaller = getUnmarshaller();
		return (JpylyzerConfig) stringUnmarshaller.unmarshal(toConvert);
	}

	static void toXml(final JpylyzerConfig toConvert, final Writer writer,
					  Boolean prettyXml) throws JAXBException {
		Marshaller varMarshaller = getMarshaller(prettyXml);
		varMarshaller.marshal(toConvert, writer);
	}

	static JpylyzerConfig fromXml(final Reader toConvert)
			throws JAXBException {
		Unmarshaller stringUnmarshaller = getUnmarshaller();
		return (JpylyzerConfig) stringUnmarshaller.unmarshal(toConvert);
	}

	private static Unmarshaller getUnmarshaller() throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(JpylyzerConfig.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return unmarshaller;
	}

	private static Marshaller getMarshaller(Boolean setPretty)
			throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(JpylyzerConfig.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, setPretty);
		return marshaller;
	}
}
