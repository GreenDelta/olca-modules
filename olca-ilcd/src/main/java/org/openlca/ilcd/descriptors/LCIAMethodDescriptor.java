package org.openlca.ilcd.descriptors;

import org.openlca.ilcd.commons.DataSetType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class LCIAMethodDescriptor extends Descriptor {

	@Override
	protected DataSetType getType() {
		return DataSetType.LCIA_METHOD;
	}

}
