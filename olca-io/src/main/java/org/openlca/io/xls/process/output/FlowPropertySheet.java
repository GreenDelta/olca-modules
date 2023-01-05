package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

import java.util.HashSet;
import java.util.Set;

class FlowPropertySheet implements EntitySheet {

	private final ProcessWorkbook wb;
	private final Set<FlowProperty> properties = new HashSet<>();

	FlowPropertySheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	@Override
	public void visit(RootEntity entity) {
		Util.flowPropertiesOf(entity, properties::add);
	}

	@Override
	public void flush() {
		if (properties.isEmpty())
			return;
		var sheet = wb.workbook.createSheet("Flow properties");
		Excel.trackSize(sheet, 0, 7);
		writeHeader(sheet);
		int row = 0;
		for (var property : properties) {
			row++;
			write(sheet, row, property);
		}
		Excel.autoSize(sheet, 0, 7);
	}

	private void writeHeader(Sheet sheet) {
		wb.header(sheet, 0, 0, "UUID");
		wb.header(sheet, 0, 1, "Name");
		wb.header(sheet, 0, 2, "Description");
		wb.header(sheet, 0, 3, "Category");
		wb.header(sheet, 0, 4, "Unit group");
		wb.header(sheet, 0, 5, "Type");
		wb.header(sheet, 0, 6, "Version");
		wb.header(sheet, 0, 7, "Last change");
	}

	private void write(Sheet sheet, int row, FlowProperty p) {
		Excel.cell(sheet, row, 0, p.refId);
		Excel.cell(sheet, row, 1, p.name);
		Excel.cell(sheet, row, 2, p.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(p.category));
		if (p.unitGroup != null) {
			Excel.cell(sheet, row, 4, p.unitGroup.name);
		}
		var type = p.flowPropertyType == FlowPropertyType.ECONOMIC
				? "Economic"
				: "Physical";
		Excel.cell(sheet, row, 5, type);
		Excel.cell(sheet, row, 6, Version.asString(p.version));
		wb.date(sheet, row, 7, p.lastChange);
	}

}
