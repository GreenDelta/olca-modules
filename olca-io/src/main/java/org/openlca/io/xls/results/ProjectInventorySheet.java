package org.openlca.io.xls.results;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.Contributions;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.openlca.io.xls.Excel;

class ProjectInventorySheet {

	private final ProjectResultExport export;
	private final Sheet sheet;

	private ProjectInventorySheet(ProjectResultExport export, Sheet sheet) {
		this.export = export;
		this.sheet = sheet;
	}

	public static void write(ProjectResultExport export, Sheet sheet) {
		new ProjectInventorySheet(export, sheet).run();
	}

	private void run() {
		Excel.trackSize(sheet, 1, 6);
		int row = 1;
		header(sheet, row++, 1, "Inventory Results");
		row = writeRows(row, true);
		row++;
		writeRows(row, false);
		Excel.autoSize(sheet, 1, 6);
	}

	private int writeRows(int row,  boolean inputs) {
		var variants = export.variants;
		header(sheet, row, 1, inputs ? "Inputs" : "Outputs");
		for (int i = 0; i < variants.length; i++) {
			int col = i + 6;
			header(sheet, row, col, variants[i].name);
		}
		row++;
		writeHeader(row++);
		for (EnviFlow flow : export.resultItems.enviFlows()) {
			if (flow.isInput())
				continue;
			writeInfo(flow.flow(), row);
			var contributions = export.result.getContributions(flow);
			for (int i = 0; i < variants.length; i++) {
				int col = i + 6;
				ProjectVariant variant = variants[i];
				Contribution<?> c = Contributions.get(contributions, variant);
				if (c == null)
					continue;
				Excel.cell(sheet, row, col, c.amount);
			}
			row++;
		}
		return row;
	}

	private void writeInfo(FlowDescriptor flow, int row) {
		int col = 1;
		Excel.cell(sheet, row, col++, flow.refId);
		Excel.cell(sheet, row, col++, flow.name);
		CategoryPair flowCat = CategoryPair.create(flow, export.cache);
		Excel.cell(sheet, row, col++, flowCat.getCategory());
		Excel.cell(sheet, row, col++, flowCat.getSubCategory());
		Excel.cell(sheet, row, col++,
				DisplayValues.referenceUnit(flow, export.cache));
	}

	private void writeHeader(int row) {
		int col = 1;
		header(sheet, row, col++, "Flow UUID");
		header(sheet, row, col++, "Flow");
		header(sheet, row, col++, "Category");
		header(sheet, row, col++, "Sub-category");
		header(sheet, row, col++, "Unit");
	}

	private void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val)
				.ifPresent(c -> c.setCellStyle(export.headerStyle));
	}

}
