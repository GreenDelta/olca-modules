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

	UNIFORM

}
