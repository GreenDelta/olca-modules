package org.openlca.io.xls.results;

import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionSet;
import org.openlca.core.results.ProjectResult;
import org.openlca.io.xls.Excel;

class ProjectImpacts {

	private ProjectResult result;
	private EntityCache cache;
	private Sheet sheet;
	private CellStyle headerStyle;

	public static void write(ProjectResult result, EntityCache cache,
			Sheet sheet, CellStyle headerStyle) {
		ProjectImpacts writer = new ProjectImpacts();
		writer.cache = cache;
		writer.result = result;
		writer.sheet = sheet;
		writer.headerStyle = headerStyle;
		writer.run();
	}

	private ProjectImpacts() {
	}

	private void run() {
		List<ProjectVariant> variants = Utils
				.sortVariants(result.getVariants());
		List<ImpactCategoryDescriptor> impacts = Utils.sortImpacts(result
				.getImpacts(cache));
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
				Excel.cell(sheet, row, col, c.getAmount());
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
