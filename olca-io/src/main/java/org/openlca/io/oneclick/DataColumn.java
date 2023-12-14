package org.openlca.io.oneclick;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.io.xls.Excel;

public enum DataColumn {

	MODULE(0, "MODULE", "Mandatory; Choose the life-cycle module"),
	CLASS(1, "CLASS", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	EPD_NUMBER(2, "EPDNUMBER", "Mandatory; Exact material/process names suggested"),
	INPUT_QUANTITY(3, "INPUT QUANTITY", "Mandatory; Amount of material, process or energy"),
	UNIT(4, "UNIT", "Mandatory; Applied unit of measurement"),
	FACTORY_LEVEL_DATA(5, "FACTORY-LEVEL DATA?", "Optional; If you inputted factory-data, write 'Yes'"),
	QUANTITY(6, "QUANTITY", "This is imported into the software"),
	MASS_PER_UNIT(7, "MASS_PER_UNIT", "Optional; Unit mass for non-energy inputs"),
	COMMENT(8, "COMMENT", "Optional. Use to explain the material, its use, or any of your own notes"),
	RESOURCE(9, "RESOURCE", "Optional. Use for data grouping"),
	TRANSPORT_LEG1_KM(10, "TRANSPORT_LEG1_KM", "Mandatory; For materials and waste treatment processes only"),
	TRANSPORT_METHOD_LEG1(11, "TRANSPORT METHOD, LEG 1", "Mandatory; For materials and waste treatment processes only"),
	TRANSPORT_LEG1_METHOD(12, "TRANSPORT_LEG1_METHOD", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	TRANSPORT_LEG2_KM(13, "TRANSPORT_LEG2_KM", "Optional; For materials and waste treatment processes only"),
	TRANSPORT_METHOD_LEG2(14, "TRANSPORT METHOD, LEG 2", "Optional; For materials and waste treatment processes only"),
	TRANSPORT_LEG2_METHOD(15, "TRANSPORT_LEG2_METHOD", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	PRODUCTION_LOSSES(16, "PRODUCTION_LOSSES", "Optional; Raw materials (A1) only"),
	END_OF_LIFE_STAGE(17, "END OF LIFE STAGE", "Mandatory; End of life (C1-C4) only"),
	EOL_STAGE(18, "EOL_STAGE", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	OUTPUT_MASS_TYPE(19, "OUTPUT MASS TYPE", "Mandatory; Modules A3, B3/B4/B5, C3 (waste flows only)"),
	OUTPUT_FLOW(20, "OUTPUT_FLOW", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	USE_FOR_A1_A2_TRACI(21, "USE FOR +A1/+A2/TRACI", "Optional, product stage (A1-A3) only"),
	USE_FOR_STANDARD(22, "USE_FOR_STANDARD", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	COPRODUCT_ALLOCATION(23, "COPRODUCT_ALLOCATION", "Optional, for raw materials (A1) only"),
	BIOGENIC_CARBON_BALANCING(24, "BIOGENIC CARBON BALANCING", "Optional; Automates biogenic carbon balancing. Can also be done manually instead."),
	BALANCING_OPTION(25, "BALANCING_OPTION", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	MODULE_C3(26, "MODULE_C3", "EOL scenario percentages recognized only if \"Balance in EOL (C3,C4)\" is chosen FOR BIOGENIC CARBON BALANCING"),
	MODULE_C4(27, "MODULE_C4", "EOL scenario percentages recognized only if \"Balance in EOL (C3,C4)\" is chosen FOR BIOGENIC CARBON BALANCING"),
	ENERGY_BALANCING(28, "ENERGY BALANCING", "Optional; Automates energy balancing. Can also be done manually instead."),
	ENERGY_BALANCING_FORMULA(29, "ENERGY_BALANCING", "Formula present in this column for software import. Please drag & drop formula if a row is inserted."),
	ENERGY_C3(30, "ENERGY_C3", "EOL scenario percentages recognized only if \"Balance in EOL (C3,C4)\" is chosen FOR ENERGY BALANCING"),
	ENERGY_C4(31, "ENERGY_C4", "EOL scenario percentages recognized only if \"Balance in EOL (C3,C4)\" is chosen FOR ENERGY BALANCING");

	private final int index;
	private final String header;
	private final String description;

	DataColumn(int index, String header, String description) {
		this.index = index;
		this.header = header;
		this.description = description;
	}

	static void writeHeadersTo(Sheet sheet) {
		for (var col : values()) {
			Excel.cell(sheet, 0, col.index, col.header);
			Excel.cell(sheet, 1, col.index, col.description);
			sheet.autoSizeColumn(col.index);
		}
	}

	void write(Row row, double value) {
		Excel.cell(row, index, value);
	}

	void write(Row row, String value) {
		Excel.cell(row, index, value);
	}
}
