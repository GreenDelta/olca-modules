package org.openlca.ecospold.internal.process;

import javax.xml.bind.JAXBElement;

import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.io.EcoSpoldXmlBinder;

public class ProcessXmlBinder extends EcoSpoldXmlBinder<EcoSpold> {

	private ObjectFactory factory = new ObjectFactory();

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
