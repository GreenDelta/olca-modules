package org.openlca.io.xls.results;

import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionSet;
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
		List<ProjectVariant> variants = Sort.variants(result.getVariants());
		List<FlowDescriptor> flows = Sort.flows(result.getFlows(), cache);
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
			List<FlowDescriptor> flows, boolean inputs) {
		header(sheet, row, 1, inputs ? "Inputs" : "Outputs");
		for (int i = 0; i < variants.size(); i++) {
			int col = i + 6;
			header(sheet, row, col, variants.get(i).getName());
		}
		row++;
		writeHeader(row++);
		FlowIndex index = result.getResult(variants.get(0)).flowIndex;
		for (FlowDescriptor flow : flows) {
			if (inputs != index.isInput(flow.getId()))
				continue;
			writeInfo(flow, row);
			ContributionSet<ProjectVariant> contributions = result
					.getContributions(flow);
			for (int i = 0; i < variants.size(); i++) {
				int col = i + 6;
				ProjectVariant variant = variants.get(i);
				ContributionItem<?> c = contributions.getContribution(variant);
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
		Excel.cell(sheet, row, col++, flow.getRefId());
		Excel.cell(sheet, row, col++, flow.getName());
		CategoryPair flowCat = CategoryPair.create(flow, result.cache);
		Excel.cell(sheet, row, col++, flowCat.getCategory());
		Excel.cell(sheet, row, col++, flowCat.getSubCategory());
		Excel.cell(sheet, row, col++,
				DisplayValues.referenceUnit(flow, result.cache));
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
		Excel.cell(sheet, row, col, val).setCellStyle(headerStyle);
	}

}
