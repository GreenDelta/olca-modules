package org.openlca.ecospold.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.openlca.ecospold.IEcoSpold;

public abstract class EcoSpoldXmlBinder<T extends IEcoSpold> {

	private HashMap<Class<?>, Marshaller> marshallers = new HashMap<>();
	private HashMap<Class<?>, Unmarshaller> unmarshallers = new HashMap<>();

	protected EcoSpoldXmlBinder() {

	}

	private Marshaller createMarshaller(Object ilcdObject) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(ilcdObject.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		return marshaller;
	}

	private Unmarshaller createUnmarshaller(Class<?> clazz)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return unmarshaller;
	}

	private Marshaller getMarshaller(Object object) throws JAXBException {
		Class<?> clazz = object.getClass();
		Marshaller marshaller = marshallers.get(clazz);
		if (marshaller != null)
			return marshaller;
		marshaller = createMarshaller(object);
		marshallers.put(clazz, marshaller);
		return marshaller;
	}

	private Unmarshaller getUnmarshaller(Class<?> clazz) throws JAXBException {
		Unmarshaller unmarshaller = unmarshallers.get(clazz);
		if (unmarshaller != null)
			return unmarshaller;
		unmarshaller = createUnmarshaller(clazz);
		unmarshallers.put(clazz, unmarshaller);
		return unmarshaller;
	}

	protected abstract Class<T> getEcoSpoldClass();

	protected abstract JAXBElement<T> toElement(IEcoSpold ecoSpold);

	void marshal(IEcoSpold ecoSpold, OutputStream outputStream)
			throws JAXBException {
		getMarshaller(ecoSpold).marshal(toElement(ecoSpold), outputStream);
	}

	T unmarshal(InputStream inputStream) throws JAXBException {
		@SuppressWarnings("unchecked")
		JAXBElement<T> element = (JAXBElement<T>) getUnmarshaller(
				getEcoSpoldClass()).unmarshal(inputStream);
		return element.getValue();
	}

}
