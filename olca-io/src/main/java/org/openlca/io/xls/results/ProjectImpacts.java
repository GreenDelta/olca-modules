package org.openlca.io.xls.results;

import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionSet;
import org.openlca.core.results.ProjectResultProvider;
import org.openlca.io.xls.Excel;

class ProjectImpacts {

	private ProjectResultProvider result;
	private Sheet sheet;
	private CellStyle headerStyle;

	public static void write(ProjectResultProvider result, Sheet sheet,
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
		List<ProjectVariant> variants = Sort.variants(result.getVariants());
		List<ImpactCategoryDescriptor> impacts = Sort.impacts(result.getImpactDescriptors());
		int row = 1;
		header(sheet, row++, 1, "LCIA Results");
		writeRows(row, variants, impacts);
		Excel.autoSize(sheet, 1, 4);
	}

	private int writeRows(int row, List<ProjectVariant> variants,
			List<ImpactCategoryDescriptor> impacts) {
		for (int i = 0; i < variants.size(); i++) {
			int col = i + 4;
			header(sheet, row, col, variants.get(i).getName());
		}
		row++;
		writeHeader(row++);
		for (ImpactCategoryDescriptor impact : impacts) {
			writeInfo(row, impact);
			ContributionSet<ProjectVariant> contributions = result
					.getContributions(impact);
			for (int i = 0; i < variants.size(); i++) {
				int col = i + 4;
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

	private void writeHeader(int row) {
		int col = 1;
		header(sheet, row, col++, "Impact category UUID");
		header(sheet, row, col++, "Impact category");
		header(sheet, row, col++, "Reference unit");
	}

	void writeInfo(int row, ImpactCategoryDescriptor impact) {
		int col = 1;
		Excel.cell(sheet, row, col++, impact.getRefId());
		Excel.cell(sheet, row, col++, impact.getName());
		Excel.cell(sheet, row, col++, impact.getReferenceUnit());
	}

	private void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val).setCellStyle(headerStyle);
	}
}
