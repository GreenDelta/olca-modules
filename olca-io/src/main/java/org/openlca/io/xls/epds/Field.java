package org.openlca.io.xls.epds;

/// A field in a sheet. The label of a field is written in the second column
/// and its value in the third column.
enum Field {

	UUID("UUID"),
	NAME("Name"),
	CATEGORY("Category"),
	VERSION("Version"),
	LAST_CHANGE("Last change"),
	TAGS("Tags"),
	DESCRIPTION("Description"),

	CODE("Code"),
	SOURCE("Source"),
	REFERENCE_UNIT("Reference unit"),
	IMPACT_DIRECTION("Impact direction"),

	RESULT("Result"),
	RESULT_MULTIPLIER("Result multiplier"),
	PRODUCT_SYSTEM("Product system"),
	LCIA_METHOD("LCIA method"),

	FLOW("Flow"),
	REFERENCE_FLOW("Reference flow"),
	AMOUNT("Amount"),
	MASS("Mass"),

	PUBLICATION_DATE("Publication date"),
	VALID_UNTIL("Valid until"),
	LOCATION("Location"),

	MANUFACTURING_DESCRIPTION("Manufacturing description"),
	PRODUCT_USAGE_DESCRIPTION("Product usage description"),

	DECLARATION_OWNER_MANUFACTURER("Declaration owner / Manufacturer"),
	PROGRAM_OPERATOR("Program operator"),
	REGISTRATION_NUMBER("Registration number"),
	ORIGINAL_EPD("Original EPD"),
	URN("URN"),

	EPD_TYPE("EPD type"),
	PCR("PCR"),
	DATA_GENERATOR("Data generator"),
	VERIFIER("Verifier"),
	USE_ADVICE("Use advice");

	private final String label;

	Field(String label) {
		this.label = label;
	}

	String label() {
		return label;
	}

	boolean matches(String s) {
		return s != null && s.strip().equalsIgnoreCase(label);
	}

	@Override
	public String toString() {
		return label;
	}
}
