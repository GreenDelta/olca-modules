package org.openlca.ecospold.internal.impact;

import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.io.EcoSpoldXmlBinder;

import jakarta.xml.bind.JAXBElement;

public class ImpactXmlBinder extends EcoSpoldXmlBinder<ImpactEcoSpold> {

	private ObjectFactory factory = new ObjectFactory();

	@Override
	public boolean matches(IEcoSpold spold) {
		return spold instanceof ImpactEcoSpold;
	}

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
