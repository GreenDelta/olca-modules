package org.openlca.io.xls.epds;

/// A section in a sheet. The header of a section is written in the first
/// column of the row where the section starts.
enum Section {

	GENERAL_INFORMATION("General information"),
	DECLARED_PRODUCT("Declared product"),
	TIME_AND_LOCATION("Time and location"),
	TECHNOLOGY("Technology"),
	PUBLICATION_AND_OWNERSHIP("Publication and ownership"),
	MODELING_AND_VALIDATION("Modeling and validation");

	private final String header;

	Section(String header) {
		this.header = header;
	}

	String header() {
		return header;
	}

	boolean matches(String s) {
		return s != null && s.strip().equalsIgnoreCase(header);
	}

	@Override
	public String toString() {
		return header;
	}
}
