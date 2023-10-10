package org.openlca.io.oneclick;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.io.xls.Excel;

enum Module {

	MATERIALS("A1 Raw materials", "PRODUCT_MATERIALS"),
	ENERGY("A3 Energy use", "PRODUCT_ENERGY"),
	PACKAGING("A3 Packaging materials", "PRODUCT_PACKAGING"),
	WASTE("A3 Manufacturing waste", "PRODUCT_WASTE");

	private final String module;
	private final String className;

	Module(String module, String className) {
		this.module = module;
		this.className = className;
	}

	void writeTo(Row row) {
		Excel.cell(row, 0, module);
		Excel.cell(row, 1, className);
	}
}

