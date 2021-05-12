package org.openlca.io.xls.results;

import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.index.IndexFlow;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.Contributions;
import org.openlca.core.results.ProjectResult;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.openlca.io.xls.Excel;

class ProjectInventories {

	private ProjectResult result;
	private Sheet sheet;
	private CellStyle headerStyle;
	private EntityCache cache;

	public static void write(ProjectResult result, Sheet sheet,
			CellStyle headerStyle, EntityCache cache) {
		ProjectInventories writer = new ProjectInventories();
		writer.result = result;
		writer.sheet = sheet;
		writer.headerStyle = headerStyle;
		writer.cache = cache;
		writer.run();
	}

	private ProjectInventories() {
	}

	private void run() {
		Excel.trackSize(sheet, 1, 6);
		List<ProjectVariant> variants = result.getVariants();
		List<IndexFlow> flows = result.getFlows();
		if (variants.isEmpty() || flows.isEmpty())
			return;
		int row = 1;
		header(sheet, row++, 1, "Inventory Results");
		row = writeRows(row, variants, flows, true);
		row++;
		writeRows(row, variants, flows, false);
		Excel.autoSize(sheet, 1, 6);
	}

	private int writeRows(int row, List<ProjectVariant> variants,
			List<IndexFlow> flows, boolean inputs) {
		header(sheet, row, 1, inputs ? "Inputs" : "Outputs");
		for (int i = 0; i < variants.size(); i++) {
			int col = i + 6;
			header(sheet, row, col, variants.get(i).name);
		}
		row++;
		writeHeader(row++);
		for (IndexFlow flow : flows) {
			if (flow.isInput())
				continue;
			writeInfo(flow.flow(), row);
			List<Contribution<ProjectVariant>> contributions = result
					.getContributions(flow);
			for (int i = 0; i < variants.size(); i++) {
				int col = i + 6;
				ProjectVariant variant = variants.get(i);
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
		CategoryPair flowCat = CategoryPair.create(flow, cache);
		Excel.cell(sheet, row, col++, flowCat.getCategory());
		Excel.cell(sheet, row, col++, flowCat.getSubCategory());
		Excel.cell(sheet, row, col++,
				DisplayValues.referenceUnit(flow, cache));
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
				.ifPresent(c -> c.setCellStyle(headerStyle));
	}

}
