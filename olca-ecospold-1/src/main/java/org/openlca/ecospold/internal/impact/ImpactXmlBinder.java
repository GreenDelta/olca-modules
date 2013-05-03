package org.openlca.ecospold.internal.impact;

import javax.xml.bind.JAXBElement;

import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.io.EcoSpoldXmlBinder;

public class ImpactXmlBinder extends EcoSpoldXmlBinder<ImpactEcoSpold> {

	private ObjectFactory factory = new ObjectFactory();

	@Override
	protected Class<ImpactEcoSpold> getEcoSpoldClass() {
		return ImpactEcoSpold.class;
	}
	
	@Override
	protected JAXBElement<ImpactEcoSpold> toElement(IEcoSpold ecoSpold) {
		JAXBElement<ImpactEcoSpold> element = null;
		if (ecoSpold instanceof ImpactEcoSpold) {
			element = factory.createEcoSpold((ImpactEcoSpold) ecoSpold);
		}
		return element;
	}

}
