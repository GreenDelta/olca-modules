package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Unit;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Flow property factors are only written when required, which is the case
 * when a flow has more properties than its reference flow property.
 */
class FlowPropertyFactorSheet {

	private final ProcessWorkbook config;
	private final Set<Flow> flows = new HashSet<>();

	FlowPropertyFactorSheet(ProcessWorkbook config) {
		this.config = config;
	}

	void put(Flow flow) {
		if (flow == null || flow.flowPropertyFactors.size() < 2)
			return;
		flows.add(flow);
	}

	void flush() {
		if (flows.isEmpty())
			return;
		var sheet = config.workbook.createSheet("Flow property factors");
		writeHeader(sheet);
		var list = new ArrayList<>(flows);
		Util.sort(list);
		int row = 0;
		for (var flow : flows) {
			for (var factor : flow.flowPropertyFactors) {
				var prop = factor.flowProperty;
				if (prop == null
						|| prop.unitGroup == null
						|| Objects.equals(prop, flow.referenceFlowProperty))
					continue;
				row++;
				Excel.cell(sheet, row, 0, flow.name);
				Excel.cell(sheet, row, 1, CategoryPath.getFull(flow.category));
				Excel.cell(sheet, row, 2, prop.name);
				Excel.cell(sheet, row, 3, factor.conversionFactor);
				Unit refUnit = prop.unitGroup.referenceUnit;
				if (refUnit != null) {
					Excel.cell(sheet, row, 4, refUnit.name);
				}
			}
		}
	}

	private void writeHeader(Sheet sheet) {
		config.header(sheet, 0, 0, "Flow");
		config.header(sheet, 0, 1, "Category");
		config.header(sheet, 0, 2, "Flow property");
		config.header(sheet, 0, 3, "Conversion factor");
		config.header(sheet, 0, 4, "Reference unit");
	}
}
