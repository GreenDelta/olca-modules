package org.openlca.ecospold.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.internal.impact.ImpactXmlBinder;
import org.openlca.ecospold.internal.process.ProcessXmlBinder;

public class EcoSpoldIO {

	private static final EcoSpoldXmlBinder<?> processXmlBinder = new ProcessXmlBinder();
	private static final EcoSpoldXmlBinder<?> impactMethodXmlBinder = new ImpactXmlBinder();

	/**
	 * Tries to detect the EcoSpold data set type from the given file. If this
	 * fails, an empty Optional is returned.
	 */
	public static Optional<DataSetType> getType(File file) {
		try (var stream = new FileInputStream(file);
			 var buff = new BufferedInputStream(stream)) {
			return getType(buff);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	/**
	 * Tries to detect the EcoSpold data set type from the given stream. If this
	 * fails, an empty Optional is returned.
	 */
	public static Optional<DataSetType> getType(InputStream stream) {

		// we just check the root element
		try {
			var reader = XMLInputFactory.newInstance()
					.createXMLStreamReader(stream);
			while (reader.hasNext()) {
				int next = reader.next();
				if (next != XMLStreamReader.START_ELEMENT)
					continue;
				var qname = reader.getName();
				if (!Objects.equals("ecoSpold", qname.getLocalPart()))
					return Optional.empty();
				var type = DataSetType.forNamespace(qname.getNamespaceURI());
				return Optional.ofNullable(type);
			}
			return Optional.empty();
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static IEcoSpold readFrom(File file, DataSetType type)
			throws Exception {
		try (var stream = new FileInputStream(file)) {
			return readFrom(stream, type);
		}
	}

	public static IEcoSpold readFrom(InputStream stream, DataSetType type)
			throws Exception {
		IEcoSpold result = null;
		switch (type) {
			case PROCESS:
				result = processXmlBinder.unmarshal(stream);
				break;
			case IMPACT_METHOD:
				result = impactMethodXmlBinder.unmarshal(stream);
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
}
