package org.openlca.ecospold.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.openlca.ecospold.IEcoSpold;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public abstract class EcoSpoldXmlBinder<T extends IEcoSpold> {

	private final HashMap<Class<?>, Marshaller> marshallers = new HashMap<>();
	private final HashMap<Class<?>, Unmarshaller> unmarshallers = new HashMap<>();

	protected EcoSpoldXmlBinder() {
	}

	/**
	 * Returns true if the given EcoSpold type matches this XML binder.
	 */
	public abstract boolean matches(IEcoSpold spold);

	private Marshaller createMarshaller(Object object) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		return marshaller;
	}

	private Unmarshaller createUnmarshaller(Class<?> clazz)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		return context.createUnmarshaller();
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
