package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.openlca.ilcd.commons.DataSetType;

@XmlAccessorType(XmlAccessType.FIELD)
public class LCIAMethodDescriptor extends Descriptor {

	@Override
	protected DataSetType getType() {
		return DataSetType.LCIA_METHOD;
	}

}
