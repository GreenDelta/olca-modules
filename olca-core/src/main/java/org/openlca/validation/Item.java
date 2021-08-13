package org.openlca.validation;

import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.LoggerFactory;

public class Item {

	public enum Type {
		ERROR,
		WARNING,
		OK,
	}

	final Type type;
	final Descriptor model;
	final String message;

	Item(Type type, Descriptor model, String message) {
		this.type = type;
		this.model = model;
		this.message = message;
	}

	static Item ok(String message) {
		return new Item(Type.OK, null, message);
	}

	static Item error(String message) {
		return new Item(Type.ERROR, null, message);
	}

	static Item error(String message, Throwable err) {
		var m = message + ": " + err.getMessage();
		var log = LoggerFactory.getLogger(Validation.class);
		log.error(message, err);
		return error(m);
	}

	static Item error(Descriptor model, String message) {
		return new Item(Type.ERROR, model, message);
	}

	static Item warning(String message) {
		return new Item(Type.WARNING, null, message);
	}

	static Item warning(Descriptor model, String message) {
		return new Item(Type.WARNING, model, message);
	}

	public boolean isError() {
		return type == Type.ERROR;
	}

	public boolean isOk() {
		return type == Type.OK;
	}

	public boolean isWarning() {
		return type == Type.WARNING;
	}

	public boolean hasModel() {
		return model != null;
	}

	public Descriptor model() {
		return model;
	}

	public Type type() {
		return type;
	}

	public String message() {
		return message;
	}

	@Override
	public String toString() {
		return model == null
			? type + ": " + message
			: type + ": " + message + "; model=" + model;
	}
}
