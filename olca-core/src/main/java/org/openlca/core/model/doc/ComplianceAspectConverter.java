package org.openlca.core.model.doc;

import jakarta.persistence.Converter;

@Converter
public class ComplianceAspectConverter extends SimpleMapConverter {

	@Override
	String keySlot() {
		return "aspect";
	}

	@Override
	String valSlot() {
		return "value";
	}

}
