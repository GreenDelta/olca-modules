package org.openlca.ilcd.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.ObjectFactory;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

/**
 * A helper class for reading and writing ILCD types from / to XML. Uses the
 * standard JAXB mechanisms but in combination with some ILCD specific things.
 * The binder can be used for multiple IO-operations, the marshalers and
 * un-marshalers for the class types are cached.
 */
public class XmlBinder {

	private HashMap<Class<?>, Marshaller> marshallers = new HashMap<>();
	private HashMap<Class<?>, Unmarshaller> unmarshallers = new HashMap<>();

	/** Writes the given ILCD object to a file. */
	public void toFile(Object ilcdObject, File file) throws JAXBException {
		getMarshaller(ilcdObject).marshal(toElement(ilcdObject), file);
	}

	/**
	 * Writes the given ILCD object to a output stream. The stream is flushed
	 * and closed within this method.
	 */
	public void toStream(Object ilcdObject, OutputStream stream)
			throws JAXBException, IOException {
		getMarshaller(ilcdObject).marshal(toElement(ilcdObject), stream);
		stream.flush();
		stream.close();
	}

	/**
	 * Writes the given ILCD object to a writer. The writer is flushed and
	 * closed within this method.
	 */
	public void toWriter(Object ilcdObject, Writer writer)
			throws JAXBException, IOException {
		getMarshaller(ilcdObject).marshal(toElement(ilcdObject), writer);
		writer.flush();
		writer.close();
	}

	public byte[] toByteArray(Object ilcdObject) throws JAXBException,
			IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		toStream(ilcdObject, os);
		return os.toByteArray();
	}

	private Marshaller getMarshaller(Object ilcdObject) throws JAXBException {
		Class<?> clazz = ilcdObject.getClass();
		Marshaller marshaller = marshallers.get(clazz);
		if (marshaller != null)
			return marshaller;
		marshaller = createMarshaller(ilcdObject);
		marshallers.put(clazz, marshaller);
		return marshaller;
	}

	private Marshaller createMarshaller(Object ilcdObject) throws JAXBException {
		JAXBContext context = null;
		if (ilcdObject instanceof Process)
			context = JAXBContext
					.newInstance(Process.class, ProductModel.class);
		else
			context = JAXBContext.newInstance(ilcdObject.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		return marshaller;
	}

	/** Reads an ILCD object of the given type from the given file. */
	public <T> T fromFile(Class<T> clazz, File file) throws JAXBException {
		StreamSource source = new StreamSource(file);
		return unmarshal(clazz, source);
	}

	/**
	 * Reads an ILCD object of the given type from the given stream. The stream
	 * is closed within this method.
	 */
	public <T> T fromStream(Class<T> clazz, InputStream stream)
			throws JAXBException, IOException {
		StreamSource source = new StreamSource(stream);
		T obj = unmarshal(clazz, source);
		stream.close();
		return obj;
	}

	/**
	 * Reads an ILCD object of the given type from the given reader. The reader
	 * is closed within this method.
	 */
	public <T> T fromReader(Class<T> clazz, Reader reader)
			throws JAXBException, IOException {
		StreamSource source = new StreamSource(reader);
		T obj = unmarshal(clazz, source);
		reader.close();
		return obj;
	}

	private <T> T unmarshal(Class<T> clazz, StreamSource source)
			throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller(clazz);
		JAXBElement<T> elem = unmarshaller.unmarshal(source, clazz);
		return elem.getValue();
	}

	private Unmarshaller getUnmarshaller(Class<?> clazz) throws JAXBException {
		Unmarshaller unmarshaller = unmarshallers.get(clazz);
		if (unmarshaller != null)
			return unmarshaller;
		unmarshaller = createUnmarshaller(clazz);
		unmarshallers.put(clazz, unmarshaller);
		return unmarshaller;
	}

	private Unmarshaller createUnmarshaller(Class<?> clazz)
			throws JAXBException {
		JAXBContext context = null;
		if (clazz.equals(Process.class))
			context = JAXBContext
					.newInstance(Process.class, ProductModel.class);
		else
			context = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return unmarshaller;
	}

	/**
	 * Wraps the given ILCD object into a JAXB element using the respective
	 * object factory method for the given type.
	 */
	public static JAXBElement<?> toElement(Object value) {
		if (value instanceof Process) {
			ObjectFactory factory = new ObjectFactory();
			return factory.createProcessDataSet((Process) value);
		} else if (value instanceof Flow) {
			org.openlca.ilcd.flows.ObjectFactory fac = new org.openlca.ilcd.flows.ObjectFactory();
			return fac.createFlowDataSet((Flow) value);
		} else if (value instanceof FlowProperty) {
			org.openlca.ilcd.flowproperties.ObjectFactory fac = new org.openlca.ilcd.flowproperties.ObjectFactory();
			return fac.createFlowPropertyDataSet((FlowProperty) value);
		} else if (value instanceof UnitGroup) {
			org.openlca.ilcd.units.ObjectFactory fac = new org.openlca.ilcd.units.ObjectFactory();
			return fac.createUnitGroupDataSet((UnitGroup) value);
		} else if (value instanceof Contact) {
			org.openlca.ilcd.contacts.ObjectFactory fac = new org.openlca.ilcd.contacts.ObjectFactory();
			return fac.createContactDataSet((Contact) value);
		} else if (value instanceof Source) {
			org.openlca.ilcd.sources.ObjectFactory fac = new org.openlca.ilcd.sources.ObjectFactory();
			return fac.createSourceDataSet((Source) value);
		} else if (value instanceof LCIAMethod) {
			org.openlca.ilcd.methods.ObjectFactory fac = new org.openlca.ilcd.methods.ObjectFactory();
			return fac.createLCIAMethodDataSet((LCIAMethod) value);
		} else if (value instanceof DescriptorList) {
			org.openlca.ilcd.descriptors.ObjectFactory fac = new org.openlca.ilcd.descriptors.ObjectFactory();
			return fac.createDataSetList((DescriptorList) value);
		} else {
			throw new IllegalArgumentException("Unsupported type " + value);
		}
	}

}
