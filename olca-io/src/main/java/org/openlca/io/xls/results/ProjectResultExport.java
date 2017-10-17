package org.openlca.io.xls.results;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ProjectResultProvider;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

public class ProjectResultExport {

	private Project project;
	private File file;
	private EntityCache cache;
	private CellStyle headerStyle;

	public ProjectResultExport(Project project, File file, EntityCache cache) {
		this.project = project;
		this.file = file;
		this.cache = cache;
		Collections.sort(project.getVariants(),
				new Comparator<ProjectVariant>() {
					@Override
					public int compare(ProjectVariant o1, ProjectVariant o2) {
						return Strings.compare(o1.getName(), o2.getName());
					}
				});
	}

	public void run(ProjectResultProvider result) throws Exception {
		Workbook workbook = new XSSFWorkbook();
		headerStyle = Excel.headerStyle(workbook);
		writeInfoSheet(workbook);
		Sheet inventorySheet = workbook.createSheet("LCI Results");
		ProjectInventories.write(result, inventorySheet, headerStyle);
		if (result.hasImpactResults()) {
			Sheet impactSheet = workbook.createSheet("LCIA Results");
			ProjectImpacts.write(result, impactSheet, headerStyle);
		}
		try (FileOutputStream fos = new FileOutputStream(file)) {
			workbook.write(fos);
		}
	}

	private void writeInfoSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet("Info");
		int row = 1;
		header(sheet, row++, 1, "Project result");
		header(sheet, row, 1, "Name:");
		Excel.cell(sheet, row++, 2, project.getName());
		header(sheet, row, 1, "Description:");
		Excel.cell(sheet, row++, 2, project.getDescription());
		header(sheet, row, 1, "LCIA Method:");
		if (project.getImpactMethodId() == null)
			Excel.cell(sheet, row++, 2, "none");
		else {
			ImpactMethodDescriptor method = cache.get(
					ImpactMethodDescriptor.class, project.getImpactMethodId());
			Excel.cell(sheet, row++, 2, method.getName());
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
		for (ProjectVariant variant : project.getVariants()) {
			Excel.cell(sheet, row, 1, variant.getName());
			Excel.cell(sheet, row, 2, variant.getProductSystem().getName());
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
			ParameterRedef redef = parameters.get(i);
			int r = row + i + 1;
			Excel.cell(sheet, r, 1, redef.getName());
			Excel.cell(sheet, r, 2, processName(redef));
			for (int j = 0; j < project.getVariants().size(); j++) {
				ProjectVariant variant = project.getVariants().get(j);
				int c = j + 3;
				if (r == (row + 1))
					Excel.cell(sheet, row, c, variant.getName()).setCellStyle(
							headerStyle);
				ParameterRedef variantRedef = findRedef(redef,
						variant.getParameterRedefs());
				if (variantRedef == null)
					continue;
				Excel.cell(sheet, r, c, variantRedef.getValue());
			}
		}
	}

	private String processName(ParameterRedef redef) {
		if (redef.getContextId() == null)
			return "global";
		ProcessDescriptor p = cache.get(ProcessDescriptor.class,
				redef.getContextId());
		if (p == null)
			return "not found: " + redef.getContextId();
		return p.getName();
	}

	private List<ParameterRedef> fetchParameters() {
		List<ParameterRedef> parameters = new ArrayList<>();
		for (ProjectVariant variant : project.getVariants()) {
			for (ParameterRedef redef : variant.getParameterRedefs()) {
				ParameterRedef contained = findRedef(redef, parameters);
				if (contained == null)
					parameters.add(redef);
			}
		}
		Collections.sort(parameters, new Comparator<ParameterRedef>() {
			@Override
			public int compare(ParameterRedef o1, ParameterRedef o2) {
				return Strings.compare(o1.getName(), o2.getName());
			}
		});
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
		return Objects.equals(redef1.getName(), redef2.getName())
				&& Objects.equals(redef1.getContextId(), redef2.getContextId());
	}

	private void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val).setCellStyle(headerStyle);
	}

}
