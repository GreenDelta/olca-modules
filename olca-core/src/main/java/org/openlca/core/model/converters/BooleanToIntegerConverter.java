package org.openlca.core.model.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
// Workaround the fact we should support both postgres and derby db
public class BooleanToIntegerConverter implements AttributeConverter<Boolean, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Boolean value) {
		return (value == null || !value) ? 0 : 1;
	}

	@Override
	public Boolean convertToEntityAttribute(Integer value) {
		return 1 == value;
	}
}
