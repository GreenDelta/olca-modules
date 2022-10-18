package org.openlca.git.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class FieldDefinition {

	final Type type;
	String name;
	final List<String> fields;
	final Function<String, Object> converter;
	String condition;
	List<String> conditionFields;
	Function<String, Boolean> meetsCondition;
	
	public static FieldDefinition firstOf(String field) {
		return new FieldDefinition(Type.FIRST, field, null);
	}

	public static FieldDefinition firstOf(String field, Function<String, Object> converter) {
		return new FieldDefinition(Type.FIRST, field, converter);
	}

	public static FieldDefinition allOf(String field) {
		return new FieldDefinition(Type.ALL, field, null);
	}

	public static FieldDefinition allOf(String field, Function<String, Object> converter) {
		return new FieldDefinition(Type.ALL, field, converter);
	}
	
	public FieldDefinition name(String name) {
		this.name = name;
		return this;
	}
	
	public FieldDefinition ifIs(String condition) {
		return ifIs(condition, null);
	}

	public FieldDefinition ifIsNot(String condition) {
		return ifIs(condition, s -> !Boolean.parseBoolean(s));
	}

	public FieldDefinition ifIs(String condition, Function<String, Boolean> converter) {
		this.condition = condition;
		this.conditionFields = condition != null ? Arrays.asList(condition.split("\\.")) : new ArrayList<>();
		this.meetsCondition = converter != null ? converter : s -> Boolean.parseBoolean(s);
		return this;
	}

	private FieldDefinition(Type type, String name, Function<String, Object> converter) {
		this.type = type;
		this.name = name;
		this.fields = Arrays.asList(name.split("\\."));
		this.converter = converter != null ? converter : s -> s;
	}

	boolean isConditional() {
		return conditionFields != null && !conditionFields.isEmpty();
	}
	
	static enum Type {
		FIRST, ALL;
	}

}