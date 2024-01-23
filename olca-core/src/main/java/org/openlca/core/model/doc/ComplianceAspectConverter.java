package org.openlca.core.model.doc;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class ComplianceAspectConverter implements
		AttributeConverter<Map<String, String>, String> {

	private final SimpleMapConverter conv = new SimpleMapConverter("aspect", "value");

	@Override
	public String convertToDatabaseColumn(Map<String, String> map) {
		return conv.convertToDatabaseColumn(map);
	}

	@Override
	public Map<String, String> convertToEntityAttribute(String dbData) {
		return conv.convertToEntityAttribute(dbData);
	}
}
