package org.openlca.core.database.references;

import org.openlca.core.model.AbstractEntity;

/** Describes a reference to an entity from a model. */
final class Ref {

	final String property;
	final Class<? extends AbstractEntity> type;
	final String nestedProperty;
	final Class<? extends AbstractEntity> nestedType;
	final String field;
	final boolean optional;

	Ref(Class<? extends AbstractEntity> type, String property, String field) {
		this(type, property, null, null, field, false);
	}

	Ref(Class<? extends AbstractEntity> type, String property,
			String field, boolean optional) {
		this(type, property, null, null, field, optional);
	}

	Ref(Class<? extends AbstractEntity> type, String property,
			Class<? extends AbstractEntity> nestedType,
			String nestedProperty, String field) {
		this(type, property, nestedType, nestedProperty, field, false);
	}

	Ref(Class<? extends AbstractEntity> type, String property,
			Class<? extends AbstractEntity> nestedType,
			String nestedProperty, String field, boolean optional) {
		this.type = type;
		this.property = property;
		this.nestedType = nestedType;
		this.nestedProperty = nestedProperty;
		this.field = field;
		this.optional = optional;
	}

	@Override
	public String toString() {
		String s = "Ref [" + type.getSimpleName() + "@";
		if (nestedProperty != null)
			s += nestedProperty + "/";
		s += property + "]";
		return s;
	}
}