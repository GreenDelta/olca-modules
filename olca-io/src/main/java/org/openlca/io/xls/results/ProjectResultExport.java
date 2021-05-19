package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ProjectResult;
import org.openlca.core.results.ResultItemView;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

public class ProjectResultExport {

	final Project project;
	final ProjectResult result;
	final ResultItemView resultItems;
	final EntityCache cache;
	final ProjectVariant[] variants;

	CellStyle headerStyle;

	public ProjectResultExport(
		Project project, ProjectResult result, IDatabase db) {
		this.project = project;
		this.result = result;
		this.resultItems = ResultItemView.of(result);
		this.cache = EntityCache.create(db);
		this.variants = project.variants.stream()
			.filter(variant -> !variant.isDisabled)
			.sorted((o1, o2) -> Strings.compare(o1.name, o2.name))
			.toArray(ProjectVariant[]::new);
	}

	public void writeTo(File file) throws IOException {
		var wb = new XSSFWorkbook();
		headerStyle = Excel.headerStyle(wb);
		writeInfoSheet(wb);
		if (variants.length != 0) {
			if (result.hasEnviFlows()) {
				var inventorySheet = wb.createSheet("LCI Results");
				ProjectInventorySheet.write(this, inventorySheet);
			}
			if (result.hasImpacts()) {
				ProjectImpactSheet.write(this, wb.createSheet("LCIA Results"));
			}
		}
		try (var fos = new FileOutputStream(file)) {
			wb.write(fos);
		}
	}

	private void writeInfoSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Info");
		Excel.trackSize(sheet, 1, 2);
		int row = 1;
		header(sheet, row++, 1, "Project result");
		header(sheet, row, 1, "Name:");
		Excel.cell(sheet, row++, 2, project.name);
		header(sheet, row, 1, "Description:");
		Excel.cell(sheet, row++, 2, project.description);
		header(sheet, row, 1, "LCIA Method:");
		if (project.impactMethod == null)
			Excel.cell(sheet, row++, 2, "none");
		else {
			Excel.cell(sheet, row++, 2, project.impactMethod.name);
		}
		row++;
		row = writeVariantTable(sheet, row);
		row++;
		writeParameterTable(sheet, row);
		Excel.autoSize(sheet, 1, 2);
	}

	private int writeVariantTable(Sheet sheet, int row) {
		header(sheet, row++, 1, "Variants");
		header(sheet, row, 1, "Name");
		header(sheet, row, 2, "Product system");
		header(sheet, row, 3, "Allocation method");
		header(sheet, row, 4, "Reference flow");
		header(sheet, row, 5, "Amount");
		header(sheet, row++, 6, "Unit");
		for (ProjectVariant variant : project.variants) {
			Excel.cell(sheet, row, 1, variant.name);
			Excel.cell(sheet, row, 2, variant.productSystem.name);
			// TODO: take data from the variants' functional unit
			// Excel.cell(sheet, row, 3, "Allocation method").setCellStyle(
			// headerStyle);
			// Excel.cell(sheet, row, 4, "Reference flow").setCellStyle(
			// headerStyle);
			// Excel.cell(sheet, row, 5, "Amount").setCellStyle(headerStyle);
			// Excel.cell(sheet, row, 6, "Unit").setCellStyle(headerStyle);
			row++;
		}
		return row;
	}

	private void writeParameterTable(Sheet sheet, int row) {
		header(sheet, row++, 1, "Parameters");
		List<ParameterRedef> parameters = fetchParameters();
		if (parameters.isEmpty()) {
			Excel.cell(sheet, row, 1, "no parameters redefined");
			return;
		}
		header(sheet, row, 1, "Name");
		header(sheet, row, 2, "Process");
		for (int i = 0; i < parameters.size(); i++) {
			var redef = parameters.get(i);
			int r = row + i + 1;
			Excel.cell(sheet, r, 1, redef.name);
			Excel.cell(sheet, r, 2, processName(redef));
			for (int j = 0; j < project.variants.size(); j++) {
				var variant = project.variants.get(j);
				int c = j + 3;
				if (r == (row + 1)) {
					Excel.cell(sheet, row, c, variant.name)
							.ifPresent(cc -> cc.setCellStyle(headerStyle));
				}
				var variantRedef = findRedef(redef, variant.parameterRedefs);
				if (variantRedef == null)
					continue;
				Excel.cell(sheet, r, c, variantRedef.value);
			}
		}
	}

	private String processName(ParameterRedef redef) {
		if (redef.contextId == null)
			return "global";
		ProcessDescriptor p = cache.get(ProcessDescriptor.class,
				redef.contextId);
		if (p == null)
			return "not found: " + redef.contextId;
		return p.name;
	}

	private List<ParameterRedef> fetchParameters() {
		List<ParameterRedef> parameters = new ArrayList<>();
		for (ProjectVariant variant : project.variants) {
			for (ParameterRedef redef : variant.parameterRedefs) {
				ParameterRedef contained = findRedef(redef, parameters);
				if (contained == null)
					parameters.add(redef);
			}
		}
		parameters.sort((o1, o2) -> Strings.compare(o1.name, o2.name));
		return parameters;
	}

	private ParameterRedef findRedef(ParameterRedef redef,
									 List<ParameterRedef> redefs) {
		for (ParameterRedef contained : redefs)
			if (eq(redef, contained))
				return contained;
		return null;
	}

	private boolean eq(ParameterRedef redef1, ParameterRedef redef2) {
		if (redef1 == redef2)
			return true;
		if (redef1 == null || redef2 == null)
			return false;
		return Objects.equals(redef1.name, redef2.name)
				&& Objects.equals(redef1.contextId, redef2.contextId);
	}

	private void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val)
				.ifPresent(c -> c.setCellStyle(headerStyle));
	}

}
