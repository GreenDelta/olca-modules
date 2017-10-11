package org.openlca.io.xls.process.output;

import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class FlowPropertySheet {

	private Config config;
	private Sheet sheet;
	private int row = 0;

	private FlowPropertySheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Flow properties");
	}

	public static void write(Config config) {
		new FlowPropertySheet(config).write();
	}

	private void write() {
		writeHeader();
		FlowPropertyDao dao = new FlowPropertyDao(config.database);
		List<FlowProperty> properties = dao.getAll();
		Collections.sort(properties, new EntitySorter());
		for (FlowProperty property : properties) {
			row++;
			write(property);
		}
		Excel.autoSize(sheet, 0, 7);
	}

	private void writeHeader() {
		config.header(sheet, row, 0, "UUID");
		config.header(sheet, row, 1, "Name");
		config.header(sheet, row, 2, "Description");
		config.header(sheet, row, 3, "Category");
		config.header(sheet, row, 4, "Unit group");
		config.header(sheet, row, 5, "Type");
		config.header(sheet, row, 6, "Version");
		config.header(sheet, row, 7, "Last change");
	}

	private void write(FlowProperty property) {
		Excel.cell(sheet, row, 0, property.getRefId());
		Excel.cell(sheet, row, 1, property.getName());
		Excel.cell(sheet, row, 2, property.getDescription());
		Excel.cell(sheet, row, 3, CategoryPath.getFull(property.getCategory()));
		if(property.getUnitGroup() != null)
			Excel.cell(sheet, row, 4, property.getUnitGroup().getName());
		String type = property.getFlowPropertyType() == FlowPropertyType.ECONOMIC ?
				"Economic" : "Physical";
		Excel.cell(sheet, row, 5, type);
		Excel.cell(sheet, row, 6, Version.asString(property.getVersion()));
		config.date(sheet, row, 7, property.getLastChange());
	}

}
