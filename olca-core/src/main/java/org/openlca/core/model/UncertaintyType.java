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
		return switch (type) {
			case LOG_NORMAL -> (byte) 1;
			case NORMAL -> (byte) 2;
			case TRIANGLE -> (byte) 3;
			case UNIFORM -> (byte) 4;
			default -> (byte) 0;
		};
	}

}
