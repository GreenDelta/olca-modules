package org.openlca.git.util;

import java.util.function.Function;

public class FieldDefinition {

	final String name;
	final String[] fields;
	final Function<String, Object> parser;

	public FieldDefinition(String name) {
		this(name, s -> s);
	}

	public FieldDefinition(String name, Function<String, Object> parser) {
		this.name = name;
		this.fields = name.split("\\.");
		this.parser = parser;
	}

}