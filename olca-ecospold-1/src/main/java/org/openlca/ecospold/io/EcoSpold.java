package org.openlca.ecospold.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

public class EcoSpold {

	private static final EcoSpoldXmlBinder<?> processXmlBinder = new ProcessXmlBinder();
	private static final EcoSpoldXmlBinder<?> impactMethodXmlBinder = new ImpactXmlBinder();

	/**
	 * Tries to detect the EcoSpold data set type from the given file. If this
	 * fails, an empty Optional is returned.
	 */
	public static Optional<DataSetType> typeOf(File file) {
		try (var stream = new FileInputStream(file);
				var buff = new BufferedInputStream(stream)) {
			return typeOf(buff);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	/**
	 * Tries to detect the EcoSpold data set type from the given stream. If this
	 * fails, an empty Optional is returned.
	 */
	public static Optional<DataSetType> typeOf(InputStream stream) {

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

	public static Optional<IEcoSpold> read(File file) {
		var type = typeOf(file);
		if (type.isEmpty())
			return Optional.empty();
		try {
			var spold = read(file, type.get());
			return Optional.of(spold);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static IEcoSpold read(File file, DataSetType type) {
		try (var stream = new FileInputStream(file);
				var buffer = new BufferedInputStream(stream)) {
			return read(buffer, type);
		} catch (Exception e) {
			throw new RuntimeException("Failed to read EcoSpold from", e);
		}
	}

	public static IEcoSpold read(InputStream stream, DataSetType type) {
		try {
			if (type == DataSetType.PROCESS)
				return processXmlBinder.unmarshal(stream);
			if (type == DataSetType.IMPACT_METHOD)
				return impactMethodXmlBinder.unmarshal(stream);
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse"
					+ " EcoSpold of type " + type, e);
		}
	}

	public static void write(File file, IEcoSpold spold) {
		try (var stream = new FileOutputStream(file);
				var buffer = new BufferedOutputStream(stream)) {
			write(buffer, spold);
		} catch (Exception e) {
			throw new RuntimeException("Failed to write "
					+ "EcoSpold to " + file, e);
		}
	}

	public static void write(OutputStream stream, IEcoSpold spold) {
		if (spold == null)
			return;
		try {
			if (processXmlBinder.matches(spold)) {
				processXmlBinder.marshal(spold, stream);
			} else if (impactMethodXmlBinder.matches(spold)) {
				impactMethodXmlBinder.marshal(spold, stream);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
