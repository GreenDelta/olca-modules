package org.openlca.io.xls.process;

import org.openlca.commons.Strings;

enum Field {

	ADDRESS("Address"),
	AMOUNT("Amount"),

	CAS("CAS"),
	CATEGORY("Category"),
	CITY("City"),
	CODE("Code"),
	COMMENT("Comment"),
	COMPLIANCE_SYSTEM("Compliance system"),
	CONVERSION_FACTOR("Conversion factor"),
	COSTS_REVENUES("Costs/Revenues"),
	COUNTRY("Country"),
	CURRENCY("Currency"),

	DATA_QUALITY_ENTRY("Data quality entry"),
	DEFAULT_FLOW_PROPERTY("Default flow property"),
	DESCRIPTION("Description"),

	E_MAIL("E-Mail"),

	FLOW("Flow"),
	FLOW_PROPERTY("Flow property"),
	FORMULA("Formula"),

	IS_REFERENCE("Is reference?"),
	IS_AVOIDED("Is avoided?"),

	LAST_CHANGE("Last change"),
	LATITUDE("Latitude"),
	LOCATION("Location"),
	LONGITUDE("Longitude"),

	MAXIMUM("Maximum"),
	MEAN_MODE("(G)Mean | Mode"),
	MINIMUM("Minimum"),

	NAME("Name"),
	PROVIDER("Provider"),

	REFERENCE_FLOW_PROPERTY("Reference flow property"),
	REFERENCE_UNIT("Reference unit"),

	SD("SD | GSD"),
	SYNONYMS("Synonyms"),

	TAGS("Tags"),
	TELEFAX("Telefax"),
	TELEPHONE("Telephone"),
	TEXT_REFERENCE("Text reference"),
	TYPE("Type"),

	UNCERTAINTY("Uncertainty"),
	UNIT("Unit"),
	UNIT_GROUP("Unit group"),
	URL("URL"),
	USE_ADVICE("Use advice"),
	UUID("UUID"),

	VERSION("Version"),
	WEBSITE("Website"),
	YEAR("Year"),
	ZIP_CODE("Zip code"),

	FLOW_SCHEMA("Flow schema"),
	PROCESS_SCHEMA("Process schema"),
	SOCIAL_SCHEMA("Social schema"),
	VALID_FROM("Valid from"),
	VALID_UNTIL("Valid until"),

	ACCESS_RESTRICTIONS("Access and use restrictions"),
	COPYRIGHT("Copyright"),
	CREATION_DATE("Creation date"),
	DATA_DOCUMENTOR("Data set documentor"),
	DATA_GENERATOR("Data set generator"),
	DATA_SET_OWNER("Data set owner"),
	INTENDED_APPLICATION("Intended application"),
	PROJECT("Project"),
	PUBLICATION("Publication"),

	DATA_COLLECTION_PERIOD("Data collection period"),
	DATA_COMPLETENESS("Data completeness"),
	DATA_SELECTION("Data selection"),
	DATA_TREATMENT("Data treatment"),
	LCI_METHOD("LCI method"),
	MODELING_CONSTANTS("Modeling constants"),
	PROCESS_TYPE("Process type"),

	REVIEW_TYPE("Review type"),
	REVIEW_REPORT("Review report"),
	REVIEW_DETAILS("Review details"),

	SAMPLING_PROCEDURE("Sampling procedure"),

	DEFAULT_ALLOCATION_METHOD("Default allocation method"),
	ECONOMIC("Economic"),
	PHYSICAL("Physical"),
	PRODUCT("Product"),
	DIRECTION("Direction"),

	VALUE("Value"),
	;

	private final String label;

	Field(String label) {
		this.label = label;
	}

	public static Field of(String label) {
		if (Strings.isBlank(label))
			return null;
		return switch (label.trim().toLowerCase()) {
			case "address" -> ADDRESS;
			case "amount" -> AMOUNT;
			case "cas", "cas number" -> CAS;
			case "category" -> CATEGORY;
			case "city" -> CITY;
			case "code" -> CODE;
			case "conversion factor" -> CONVERSION_FACTOR;
			case "costs/revenues" -> COSTS_REVENUES;
			case "country" -> COUNTRY;
			case "currency" -> CURRENCY;
			case "data quality entry" -> DATA_QUALITY_ENTRY;
			case "default flow property" -> DEFAULT_FLOW_PROPERTY;
			case "description" -> DESCRIPTION;
			case "e-mail", "email" -> E_MAIL;
			case "flow" -> FLOW;
			case "flow property" -> FLOW_PROPERTY;
			case "formula", "chemical formula" -> FORMULA;
			case "(g)mean | mode", "mean", "mode" -> MEAN_MODE;
			case "is avoided?" -> IS_AVOIDED;
			case "is reference?" -> IS_REFERENCE;
			case "last change" -> LAST_CHANGE;
			case "latitude" -> LATITUDE;
			case "location" -> LOCATION;
			case "longitude" -> LONGITUDE;
			case "maximum" -> MAXIMUM;
			case "minimum" -> MINIMUM;
			case "name" -> NAME;
			case "provider" -> PROVIDER;
			case "reference flow property" -> REFERENCE_FLOW_PROPERTY;
			case "reference unit" -> REFERENCE_UNIT;
			case "sd | gsd" -> SD;
			case "synonyms" -> SYNONYMS;
			case "tags" -> TAGS;
			case "telefax" -> TELEFAX;
			case "telephone" -> TELEPHONE;
			case "text reference" -> TEXT_REFERENCE;
			case "type" -> TYPE;
			case "uncertainty" -> UNCERTAINTY;
			case "unit" -> UNIT;
			case "unit group" -> UNIT_GROUP;
			case "url" -> URL;
			case "uuid", "id", "ref_id" -> UUID;
			case "version" -> VERSION;
			case "website" -> WEBSITE;
			case "year" -> YEAR;
			case "zip code" -> ZIP_CODE;
			case "flow schema" -> FLOW_SCHEMA;
			case "process schema" -> PROCESS_SCHEMA;
			case "social schema" -> SOCIAL_SCHEMA;
			case "valid from" -> VALID_FROM;
			case "valid until" -> VALID_UNTIL;

			case "access and use restrictions" -> ACCESS_RESTRICTIONS;
			case "copyright" -> COPYRIGHT;
			case "creation date" -> CREATION_DATE;
			case "data set documentor" -> DATA_DOCUMENTOR;
			case "data set generator" -> DATA_GENERATOR;
			case "data set owner" -> DATA_SET_OWNER;
			case "intended application" -> INTENDED_APPLICATION;
			case "project" -> PROJECT;
			case "publication" -> PUBLICATION;

			case "data collection period" -> DATA_COLLECTION_PERIOD;
			case "data completeness" -> DATA_COMPLETENESS;
			case "data selection" -> DATA_SELECTION;
			case "data treatment" -> DATA_TREATMENT;
			case "lci method" -> LCI_METHOD;
			case "modeling constants" -> MODELING_CONSTANTS;
			case "process type" -> PROCESS_TYPE;

			case "review type" -> REVIEW_TYPE;
			case "review report" -> REVIEW_REPORT;
			case "review details" -> REVIEW_DETAILS;

			case "sampling procedure" -> SAMPLING_PROCEDURE;

			case "default allocation method" -> DEFAULT_ALLOCATION_METHOD;
			case "economic" -> ECONOMIC;
			case "physical" -> PHYSICAL;
			case "product" -> PRODUCT;
			case "direction" -> DIRECTION;

			case "value" -> VALUE;

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

	boolean matches(String s) {
		return s != null && s.strip().equalsIgnoreCase(label);
	}
}
