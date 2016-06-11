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
@XmlRootElement(namespace = "http://www.verapdf.org/OTSConfig", name = "otsConfig")
final class OTSConfig {

	@XmlElement
	private final String cliPath;

	private OTSConfig() {
		this("");
	}

	private OTSConfig(String cliPath) {
		this.cliPath = cliPath;
	}

	public String getCliPath() {
		return cliPath;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OTSConfig otsConfig = (OTSConfig) o;

		return cliPath != null ? cliPath.equals(otsConfig.cliPath) : otsConfig.cliPath == null;

	}

	@Override
	public int hashCode() {
		return cliPath != null ? cliPath.hashCode() : 0;
	}

	static OTSConfig defaultInstance() {
		return new OTSConfig(null);
	}

	static OTSConfig fromValues(final String cliPath) {
		return new OTSConfig(cliPath);
	}

	static String toXml(final OTSConfig toConvert, Boolean prettyXml)
			throws JAXBException, IOException {
		String retVal = "";
		try (StringWriter writer = new StringWriter()) {
			toXml(toConvert, writer, prettyXml);
			retVal = writer.toString();
			return retVal;
		}
	}

	static OTSConfig fromXml(final String toConvert)
			throws JAXBException {
		try (StringReader reader = new StringReader(toConvert)) {
			return fromXml(reader);
		}
	}

	static void toXml(final OTSConfig toConvert,
					  final OutputStream stream, Boolean prettyXml) throws JAXBException {
		Marshaller varMarshaller = getMarshaller(prettyXml);
		varMarshaller.marshal(toConvert, stream);
	}

	static OTSConfig fromXml(final InputStream toConvert)
			throws JAXBException {
		Unmarshaller stringUnmarshaller = getUnmarshaller();
		return (OTSConfig) stringUnmarshaller.unmarshal(toConvert);
	}

	static void toXml(final OTSConfig toConvert, final Writer writer,
					  Boolean prettyXml) throws JAXBException {
		Marshaller varMarshaller = getMarshaller(prettyXml);
		varMarshaller.marshal(toConvert, writer);
	}

	static OTSConfig fromXml(final Reader toConvert)
			throws JAXBException {
		Unmarshaller stringUnmarshaller = getUnmarshaller();
		return (OTSConfig) stringUnmarshaller.unmarshal(toConvert);
	}

	private static Unmarshaller getUnmarshaller() throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(OTSConfig.class);
		return context.createUnmarshaller();
	}

	private static Marshaller getMarshaller(Boolean setPretty)
			throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(OTSConfig.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, setPretty);
		return marshaller;
	}
}
