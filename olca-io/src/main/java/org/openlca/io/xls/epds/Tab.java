package org.openlca.io.xls.epds;

/// A sheet (tab) in an EPD workbook.
enum Tab {

	EPD_INFORMATION("EPD - Information"),
	EPD_RESULTS("EPD - Results"),
	EPD_MODULES("EPD - Modules"),
	INDICATORS("Indicators"),
	SOURCES("Sources"),
	CONTACTS("Contacts");

	private final String label;

	Tab(String label) {
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
