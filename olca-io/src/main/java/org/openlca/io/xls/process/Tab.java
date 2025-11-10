package org.openlca.io.xls.process;

/**
 * A tab (sheet) in a process workbook.
 */
enum Tab {

	ACTORS("Actors"),
	ALLOCATION("Allocation"),
	COMPLIANCE_DECLARATIONS("Compliance declarations"),
	CURRENCIES("Currencies"),
	DOCUMENTATION("Documentation"),
	FLOW_PROPERTIES("Flow properties"),
	FLOW_PROPERTY_FACTORS("Flow property factors"),
	FLOWS("Flows"),
	GENERAL_INFO("General information"),
	INPUTS("Inputs"),
	LOCATIONS("Locations"),
	OUTPUTS("Outputs"),
	PARAMETERS("Parameters"),
	PROVIDERS("Providers"),
	REVIEWS("Reviews"),
	SOURCES("Sources"),
	UNIT_GROUPS("Unit groups"),
	UNITS("Units");

	private final String label;

	Tab(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}
}
