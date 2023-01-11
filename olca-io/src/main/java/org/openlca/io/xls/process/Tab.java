package org.openlca.io.xls.process;

public enum Tab {

	ACTORS("Actors"),
	ADMINISTRATIVE_INFORMATION("Administrative information"),
	ALLOCATION("Allocation"),
	FLOW_PROPERTIES("Flow properties"),
	FLOW_PROPERTY_FACTORS("Flow property factors"),
	FLOWS("Flows"),
	GENERAL_INFORMATION("General information"),
	INPUTS("Inputs"),
	LOCATIONS("Locations"),
	MODELING_AND_VALIDATION("Modeling and validation"),
	OUTPUTS("Outputs"),
	PARAMETERS("Parameters"),
	PROVIDERS("Providers"),
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
