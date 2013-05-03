package org.openlca.ecospold.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.internal.impact.ImpactXmlBinder;
import org.openlca.ecospold.internal.process.ProcessXmlBinder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EcoSpoldIO {

	private static SAXParser parser;
	private static EcoSpoldXmlBinder<?> processXmlBinder = new ProcessXmlBinder();
	private static EcoSpoldXmlBinder<?> impactMethodXmlBinder = new ImpactXmlBinder();

	public static DataSetType getEcoSpoldType(File file) throws Exception {
		return getEcoSpoldType(new FileInputStream(file));
	}

	public static DataSetType getEcoSpoldType(InputStream inputStream)
			throws Exception {
		SAXParser parser = getParser();
		// sax parser closes stream
		TypeHandler handler = new TypeHandler();
		parser.parse(inputStream, handler);
		return handler.type;
	}

	private static SAXParser getParser() {
		if (parser != null)
			return parser;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newSAXParser();
			return parser;
		} catch (Exception e) {
			throw new RuntimeException("Could not create parser", e);
		}
	}

	public static IEcoSpold readFrom(File file, DataSetType ecoSpoldType)
			throws Exception {
		try (InputStream inputStream = new FileInputStream(file)) {
			IEcoSpold ecoSpold = readFrom(inputStream, ecoSpoldType);
			return ecoSpold;
		}
	}

	public static IEcoSpold readFrom(InputStream inputStream,
			DataSetType ecoSpoldType) throws Exception {
		IEcoSpold result = null;
		switch (ecoSpoldType) {
		case PROCESS:
			result = processXmlBinder.unmarshal(inputStream);
			break;
		case IMPACT_METHOD:
			result = impactMethodXmlBinder.unmarshal(inputStream);
			break;
		}
		return result;
	}

	public static void writeTo(File file, IEcoSpold ecoSpold,
			DataSetType ecoSpoldType) throws Exception {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			writeTo(outputStream, ecoSpold, ecoSpoldType);
		}
	}

	public static void writeTo(OutputStream outputStream, IEcoSpold ecoSpold,
			DataSetType ecoSpoldType) throws Exception {
		if (ecoSpold == null) {
			throw new IllegalArgumentException("EcoSpold cannot be null");
		}
		switch (ecoSpoldType) {
		case PROCESS:
			processXmlBinder.marshal(ecoSpold, outputStream);
			break;
		case IMPACT_METHOD:
			impactMethodXmlBinder.marshal(ecoSpold, outputStream);
			break;
		}
	}

	private static class TypeHandler extends DefaultHandler {

		private DataSetType type;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if ("ecoSpold".equals(qName)) {
				type = DataSetType.forNamespace(uri);
			}
		}
	}

}
