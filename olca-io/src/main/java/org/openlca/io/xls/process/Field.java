package org.openlca.io.xls.process;

import org.openlca.util.Strings;

public enum Field {

	ADDRESS("Address"),

	CAS("CAS"),
	CATEGORY("Category"),
	CITY("City"),
	CODE("Code"),
	COUNTRY("Country"),
	CONVERSION_FACTOR("Conversion factor"),

	DEFAULT_FLOW_PROPERTY("Default flow property"),
	DESCRIPTION("Description"),

	E_MAIL("E-Mail"),

	FLOW("Flow"),
	FLOW_PROPERTY("Flow property"),
	FORMULA("Formula"),

	LAST_CHANGE("Last change"),
	LATITUDE("Latitude"),
	LOCATION("Location"),
	LONGITUDE("Longitude"),

	NAME("Name"),

	REFERENCE_FLOW_PROPERTY("Reference flow property"),
	REFERENCE_UNIT("Reference unit"),

	SYNONYMS("Synonyms"),

	TAGS("Tags"),
	TELEFAX("Telefax"),
	TELEPHONE("Telephone"),
	TEXT_REFERENCE("Text reference"),
	TYPE("Type"),

	UNIT_GROUP("Unit group"),
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
			case "cas", "cas number" -> CAS;
			case "category" -> CATEGORY;
			case "city" -> CITY;
			case "code" -> CODE;
			case "conversion factor" -> CONVERSION_FACTOR;
			case "country" -> COUNTRY;
			case "default flow property" -> DEFAULT_FLOW_PROPERTY;
			case "description" -> DESCRIPTION;
			case "e-mail", "email" -> E_MAIL;
			case "flow" -> FLOW;
			case "flow property" -> FLOW_PROPERTY;
			case "formula", "chemical formula" -> FORMULA;
			case "name" -> NAME;
			case "last change" -> LAST_CHANGE;
			case "latitude" -> LATITUDE;
			case "location" -> LOCATION;
			case "longitude" -> LONGITUDE;
			case "reference flow property" -> REFERENCE_FLOW_PROPERTY;
			case "reference unit" -> REFERENCE_UNIT;
			case "synonyms" -> SYNONYMS;
			case "tags" -> TAGS;
			case "telefax" -> TELEFAX;
			case "telephone" -> TELEPHONE;
			case "text reference" -> TEXT_REFERENCE;
			case "type" -> TYPE;
			case "unit group" -> UNIT_GROUP;
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
