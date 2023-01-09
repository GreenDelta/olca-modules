package org.openlca.io.xls.process;

import org.openlca.util.Strings;

public enum Field {

	ADDRESS("Address"),

	CATEGORY("Category"),
	CITY("City"),
	CODE("Code"),
	COUNTRY("Country"),

	DEFAULT_FLOW_PROPERTY("Default flow property"),
	DESCRIPTION("Description"),

	E_MAIL("E-Mail"),

	LAST_CHANGE("Last change"),
	LATITUDE("Latitude"),
	LONGITUDE("Longitude"),

	NAME("Name"),
	REFERENCE_UNIT("Reference unit"),

	TAGS("Tags"),
	TELEFAX("Telefax"),
	TELEPHONE("Telephone"),
	TEXT_REFERENCE("Text reference"),

	URL("URL"),
	UUID("UUID"),

	VERSION("Version"),
	WEBSITE("Website"),
	YEAR("Year"),
	ZIP_CODE("Zip code"),
	;

	private final String label;

	Field(String label) {
		this.label = label;
	}

	public static Field of(String label) {
		if (Strings.nullOrEmpty(label))
			return null;
		return switch (label.trim().toLowerCase()) {
			case "address" -> ADDRESS;
			case "category" -> CATEGORY;
			case "city" -> CITY;
			case "code" -> CODE;
			case "country" -> COUNTRY;
			case "default flow property" -> DEFAULT_FLOW_PROPERTY;
			case "description" -> DESCRIPTION;
			case "e-mail", "email" -> E_MAIL;
			case "name" -> NAME;
			case "last change" -> LAST_CHANGE;
			case "latitude" -> LATITUDE;
			case "longitude" -> LONGITUDE;
			case "reference unit" -> REFERENCE_UNIT;
			case "tags" -> TAGS;
			case "telefax" -> TELEFAX;
			case "telephone" -> TELEPHONE;
			case "text reference" -> TEXT_REFERENCE;
			case "url" -> URL;
			case "uuid", "id", "ref_id" -> UUID;
			case "version" -> VERSION;
			case "website" -> WEBSITE;
			case "year" -> YEAR;
			case "zip code" -> ZIP_CODE;
			default -> null;
		};
	}

	public String label() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}
}
