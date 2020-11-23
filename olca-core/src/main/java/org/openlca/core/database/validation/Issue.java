package org.openlca.core.database.validation;

import org.openlca.core.model.descriptors.Descriptor;

public class Issue {

	public enum Type {
		ERROR, WARNING
	}

	public final Type type;
	public final Descriptor model;
	public final String message;

	Issue (Type type, Descriptor model, String message) {
		this.type = type;
		this.model = model;
		this.message = message;
	}

	static Issue error(Descriptor model, String message) {
		return new Issue(Type.ERROR, model, message);
	}

	static Issue warning(Descriptor model, String message) {
		return new Issue(Type.WARNING, model, message);
	}
}
