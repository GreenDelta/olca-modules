package org.openlca.io.xls.results;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.Contribution;
import org.openlca.core.results.Contributions;
import org.openlca.io.xls.Excel;

class ProjectImpactSheet {

	private final ProjectResultExport export;
	private final CellStyle headerStyle;
	private final Sheet sheet;

	private ProjectImpactSheet(ProjectResultExport export, Sheet sheet) {
		this.export = export;
		this.headerStyle = export.headerStyle;
		this.sheet = sheet;
	}

	public static void write(ProjectResultExport export, Sheet sheet) {
		new ProjectImpactSheet(export, sheet).run();
	}

	private void run() {
		Excel.trackSize(sheet, 1, 4);
		int row = 1;
		header(sheet, row++, 1, "LCIA Results");
		writeRows(row);
		Excel.autoSize(sheet, 1, 4);
	}

	private void writeRows(int row) {
		var variants = export.variants;
		for (int i = 0; i < variants.length; i++) {
			int col = i + 4;
			header(sheet, row, col, variants[i].name);
		}
		row++;
		writeHeader(row++);
		for (var impact : export.resultItems.impacts()) {
			writeInfo(row, impact);
			// TODO: this is unnecessary complicated
			var contributions = export.result.getContributions(impact);
			for (int i = 0; i <  variants.length; i++) {
				int col = i + 4;
				ProjectVariant variant = variants[i];
				Contribution<?> c = Contributions.get(contributions, variant);
				if (c == null)
					continue;
				Excel.cell(sheet, row, col, c.amount);
			}
			row++;
		}
	}

	private void writeHeader(int row) {
		header(sheet, row, 1, "Impact category UUID");
		header(sheet, row, 2, "Impact category");
		header(sheet, row, 3, "Reference unit");
	}

	void writeInfo(int row, ImpactDescriptor impact) {
		Excel.cell(sheet, row, 1, impact.refId);
		Excel.cell(sheet, row, 2, impact.name);
		Excel.cell(sheet, row, 3, impact.referenceUnit);
	}

	private void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val)
				.ifPresent(c -> c.setCellStyle(headerStyle));
	}
}
