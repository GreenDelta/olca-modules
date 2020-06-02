package org.openlca.io.xls.results;

import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.Contributions;
import org.openlca.core.results.ProjectResult;
import org.openlca.io.xls.Excel;

class ProjectImpacts {

	private ProjectResult result;
	private Sheet sheet;
	private CellStyle headerStyle;

	public static void write(ProjectResult result, Sheet sheet,
			CellStyle headerStyle) {
		ProjectImpacts writer = new ProjectImpacts();
		writer.result = result;
		writer.sheet = sheet;
		writer.headerStyle = headerStyle;
		writer.run();
	}

	private ProjectImpacts() {
	}

	private void run() {
		int row = 1;
		header(sheet, row++, 1, "LCIA Results");
		writeRows(row, result.getVariants());
		Excel.autoSize(sheet, 1, 4);
	}

	private int writeRows(int row, List<ProjectVariant> variants) {
		for (int i = 0; i < variants.size(); i++) {
			int col = i + 4;
			header(sheet, row, col, variants.get(i).name);
		}
		row++;
		writeHeader(row++);
		for (ImpactCategoryDescriptor impact : result.getImpacts()) {
			writeInfo(row, impact);
			List<Contribution<ProjectVariant>> contributions = result
					.getContributions(impact);
			for (int i = 0; i < variants.size(); i++) {
				int col = i + 4;
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

	private void writeHeader(int row) {
		int col = 1;
		header(sheet, row, col++, "Impact category UUID");
		header(sheet, row, col++, "Impact category");
		header(sheet, row, col++, "Reference unit");
	}

	void writeInfo(int row, ImpactCategoryDescriptor impact) {
		int col = 1;
		Excel.cell(sheet, row, col++, impact.refId);
		Excel.cell(sheet, row, col++, impact.name);
		Excel.cell(sheet, row, col++, impact.referenceUnit);
	}

	private void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val).setCellStyle(headerStyle);
	}
}
