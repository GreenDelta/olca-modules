package org.openlca.ecospold.internal.process;

import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.io.EcoSpoldXmlBinder;

import jakarta.xml.bind.JAXBElement;

public class ProcessXmlBinder extends EcoSpoldXmlBinder<EcoSpold> {

	private ObjectFactory factory = new ObjectFactory();

	@Override
	public boolean matches(IEcoSpold spold) {
		return spold instanceof EcoSpold;
	}

	@Override
	protected Class<EcoSpold> getEcoSpoldClass() {
		return EcoSpold.class;
	}

	@Override
	protected JAXBElement<EcoSpold> toElement(IEcoSpold ecoSpold) {
		JAXBElement<EcoSpold> element = null;
		if (ecoSpold instanceof EcoSpold) {
			element = factory.createEcoSpold((EcoSpold) ecoSpold);
		}
		return element;
	}

}
