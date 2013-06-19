package org.openlca.core.database;

/** Content types of a (new) database. */
public enum DatabaseContent {

	/** No data. */
	EMPTY,

	/** Units and flow properties. */
	UNITS,

	/** All reference data: units, flow properties, and flows. */
	ALL_REF_DATA

}
