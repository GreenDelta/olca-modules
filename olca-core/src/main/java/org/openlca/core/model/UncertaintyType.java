package org.openlca.core.model;

/**
 * The uncertainty distribution types that are currently supported by
 * openLCA. Note that these types are currently stored by their ordinal
 * values in the database so DO NOT REORDER THEM.
 */
public enum UncertaintyType {

	NONE,

	LOG_NORMAL,

	NORMAL,

	TRIANGLE,

	UNIFORM;

	public static byte byteIndexOf(UncertaintyType type) {
		if (type == null || type == NONE)
			return 0;
		switch (type) {
			case LOG_NORMAL:
				return 1;
			case NORMAL:
				return 2;
			case TRIANGLE:
				return 3;
			case UNIFORM:
				return 4;
			default:
				return 0;
		}
	}

}
