package org.openlca.ecospold2;

import org.jdom2.Namespace;

class IO {

	/**
	 * Format pattern for XML date and time fields.
	 */
	public static final String XML_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * Format pattern for XML date fields.
	 */
	public static final String XML_DATE = "yyyy-MM-dd";

	/**
	 * Standard EcoSpold 02 namespace.
	 */
	static final Namespace NS = Namespace
			.getNamespace("http://www.EcoInvent.org/EcoSpold02");

	/**
	 * Namespace for openLCA extensions.
	 */
	static final Namespace EXT_NS = Namespace
			.getNamespace("http://openlca.org/ecospold2-extensions");

	/**
	 * Namespace for EcoSpold 02 master data in activity data sets.
	 */
	static final Namespace MD_NS = Namespace
			.getNamespace("http://www.EcoInvent.org/UsedUserMasterData");

}
