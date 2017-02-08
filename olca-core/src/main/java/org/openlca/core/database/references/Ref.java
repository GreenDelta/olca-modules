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
	// in some cases the reference is not directly set in the model but only the
	// long id (e.g. defaultProvider in processes)
	// In that cases 0 needs to be handled as null, in other cases 0 means a
	// broken reference
	final boolean longReference;

	Ref(Class<? extends AbstractEntity> type, String property, String field) {
		this(type, property, null, null, field, false, false);
	}

	Ref(Class<? extends AbstractEntity> type, String property, String field, boolean optional) {
		this(type, property, null, null, field, optional, false);
	}

	Ref(Class<? extends AbstractEntity> type, String property, String field, boolean optional, boolean longReference) {
		this(type, property, null, null, field, optional, longReference);
	}

	Ref(Class<? extends AbstractEntity> type, String property, Class<? extends AbstractEntity> nestedType,
			String nestedProperty, String field) {
		this(type, property, nestedType, nestedProperty, field, false, false);
	}

	Ref(Class<? extends AbstractEntity> type, String property, Class<? extends AbstractEntity> nestedType,
			String nestedProperty, String field, boolean optional) {
		this(type, property, nestedType, nestedProperty, field, optional, false);
	}

	Ref(Class<? extends AbstractEntity> type, String property, Class<? extends AbstractEntity> nestedType,
			String nestedProperty, String field, boolean optional, boolean longReference) {
		this.type = type;
		this.property = property;
		this.nestedType = nestedType;
		this.nestedProperty = nestedProperty;
		this.field = field;
		this.optional = optional;
		this.longReference = longReference;
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