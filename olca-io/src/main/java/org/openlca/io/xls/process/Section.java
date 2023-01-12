package org.openlca.io.xls.process;

/**
 * A section in a sheet. A section has a header and this header is the
 * identifier of the section in a sheet. The header is located in the first
 * column of the row where the section starts. The rows that follow the header
 * are the section content. The first subsequent row with no content in the
 * first column marks the end of the section.
 */
public enum Section {

	ADMINISTRATIVE_INFO("Administrative information"),
	CALCULATED_PARAMETERS("Calculated parameters"),
	CAUSAL_ALLOCATION("Causal allocation"),
	DATA_QUALITY("Data quality"),
	DATA_SOURCE_INFO("Data source information"),
	GENERAL_INFO("General information"),
	GEOGRAPHY("Geography"),
	GLOBAL_CALCULATED_PARAMETERS("Global calculated parameters"),
	GLOBAL_INPUT_PARAMETERS("Global input parameters"),
	INPUT_PARAMETERS("Input parameters"),
	MODELING_AND_VALIDATION("Modeling and validation"),
	PHYSICAL_ECONOMIC_ALLOCATION("Physical & economic allocation"),
	REVIEW("Process evaluation and validation"),
	SOURCES("Sources"),
	TECHNOLOGY("Technology"),
	TIME("Time");

	private final String header;

	Section(String header) {
		this.header = header;
	}

	public String header() {
		return header;
	}

	@Override
	public String toString() {
		return header;
	}
}
