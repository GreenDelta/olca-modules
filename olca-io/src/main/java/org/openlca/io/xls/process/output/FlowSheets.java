package org.openlca.io.xls.process.output;

import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

/**
 * Writes the flow and flow property factor sheet at the same time.
 */
class FlowSheets {

	private Config config;
	private Sheet flowSheet;
	private Sheet factorSheet;
	private int flowRow = 0;
	private int factorRow = 0;

	private FlowSheets(Config config) {
		this.config = config;
		flowSheet = config.workbook.createSheet("Flows");
		factorSheet = config.workbook.createSheet("Flow property factors");
	}

	public static void write(Config config) {
		new FlowSheets(config).write();
	}

	private void write() {
		writeFlowHeader();
		writeFactorHeader();
		FlowDao dao = new FlowDao(config.database);
		List<Flow> flows = dao.getAll();
		Collections.sort(flows, new EntitySorter());
		for (Flow flow : flows) {
			flowRow++;
			write(flow);
			for (FlowPropertyFactor factor : flow.getFlowPropertyFactors()) {
				factorRow++;
				writeFactor(flow, factor);
			}
		}
		Excel.autoSize(flowSheet, 0, 10);
		Excel.autoSize(factorSheet, 0, 4);
	}

	private void writeFlowHeader() {
		config.header(flowSheet, flowRow, 0, "UUID");
		config.header(flowSheet, flowRow, 1, "Name");
		config.header(flowSheet, flowRow, 2, "Description");
		config.header(flowSheet, flowRow, 3, "Category");
		config.header(flowSheet, flowRow, 4, "Version");
		config.header(flowSheet, flowRow, 5, "Last change");
		config.header(flowSheet, flowRow, 6, "Type");
		config.header(flowSheet, flowRow, 7, "CAS");
		config.header(flowSheet, flowRow, 8, "Formula");
		config.header(flowSheet, flowRow, 9, "Location");
		config.header(flowSheet, flowRow, 10, "Reference flow property");
	}

	private void write(Flow flow) {
		Excel.cell(flowSheet, flowRow, 0, flow.getRefId());
		Excel.cell(flowSheet, flowRow, 1, flow.getName());
		Excel.cell(flowSheet, flowRow, 2, flow.getDescription());
		Excel.cell(flowSheet, flowRow, 3, CategoryPath.getFull(flow.getCategory()));
		Excel.cell(flowSheet, flowRow, 4, Version.asString(flow.getVersion()));
		config.date(flowSheet, flowRow, 5, flow.getLastChange());
		Excel.cell(flowSheet, flowRow, 6, getType(flow));
		Excel.cell(flowSheet, flowRow, 7, flow.getCasNumber());
		Excel.cell(flowSheet, flowRow, 8, flow.getFormula());
		if (flow.getLocation() != null)
			Excel.cell(flowSheet, flowRow, 9, flow.getLocation().getName());
		if (flow.getReferenceFlowProperty() != null)
			Excel.cell(flowSheet, flowRow, 10,
					flow.getReferenceFlowProperty().getName());
	}

	private String getType(Flow flow) {
		if (flow.getFlowType() == null)
			return "Elementary flow";
		switch (flow.getFlowType()) {
			case ELEMENTARY_FLOW:
				return "Elementary flow";
			case PRODUCT_FLOW:
				return "Product flow";
			case WASTE_FLOW:
				return "Waste flow";
			default:
				return "Elementary flow";
		}
	}

	private void writeFactorHeader() {
		config.header(factorSheet, factorRow, 0, "Flow");
		config.header(factorSheet, factorRow, 1, "Category");
		config.header(factorSheet, factorRow, 2, "Flow property");
		config.header(factorSheet, factorRow, 3, "Conversion factor");
		config.header(factorSheet, factorRow, 4, "Reference unit");
	}

	private void writeFactor(Flow flow, FlowPropertyFactor factor) {
		Excel.cell(factorSheet, factorRow, 0, flow.getName());
		Excel.cell(factorSheet, factorRow, 1, CategoryPath.getFull(flow.getCategory()));
		FlowProperty prop = factor.getFlowProperty();
		if (prop != null)
			Excel.cell(factorSheet, factorRow, 2, prop.getName());
		Excel.cell(factorSheet, factorRow, 3, factor.getConversionFactor());
		if (prop == null || prop.getUnitGroup() == null)
			return;
		Unit refUnit = prop.getUnitGroup().getReferenceUnit();
		if (refUnit != null)
			Excel.cell(factorSheet, factorRow, 4, refUnit.getName());
	}
}
