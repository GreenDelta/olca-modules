package org.openlca.io.xls.process.input;

import org.openlca.util.Strings;

enum Field {

	ADDRESS,
	CATEGORY,
	CITY,
	COUNTRY,
	DESCRIPTION,
	E_MAIL,
	LAST_CHANGE,
	NAME,
	TELEFAX,
	TELEPHONE,
	UUID,
	VERSION,
	WEBSITE,
	ZIP_CODE,
	;

	static Field of(String label) {
		if (Strings.nullOrEmpty(label))
			return null;
		return switch (label.trim().toLowerCase()) {
			case "address" -> ADDRESS;
			case "category" -> CATEGORY;
			case "city" -> CITY;
			case "country" -> COUNTRY;
			case "description" -> DESCRIPTION;
			case "e-mail", "email" -> E_MAIL;
			case "name" -> NAME;
			case "last change" -> LAST_CHANGE;
			case "telefax" -> TELEFAX;
			case "telephone" -> TELEPHONE;
			case "uuid", "id", "ref_id" -> UUID;
			case "version" -> VERSION;
			case "website" -> WEBSITE;
			case "zip code" -> ZIP_CODE;
			default -> null;
		};
	}
}
