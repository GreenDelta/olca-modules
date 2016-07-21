package org.openlca.io.xls.results;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProductSystem;
import org.openlca.io.xls.Excel;

class InfoSheet {

	private CellStyle headerStyle;
	private Sheet sheet;

	private InfoSheet() {
	}

	static void write(Workbook wb, CalculationSetup setup, String title) {
		new InfoSheet().doIt(wb, setup, title);
	}

	private void doIt(Workbook wb, CalculationSetup setup, String title) {
		sheet = wb.createSheet("Calculation setup");
		headerStyle = Excel.headerStyle(wb);
		row(1, title);
		if (setup == null || setup.productSystem == null)
			return;
		ProductSystem system = setup.productSystem;
		row(2, "Product system:", system.getName());
		row(3, "Product:", product(system));
		row(4, "Amount:", amount(system));
		row(5, "LCIA Method:", method(setup));
		row(6, "Normalisation & weighting set:", nwset(setup));
		row(7, "Allocation method:", allocation(setup));
		row(8, "Date:");
		Cell dataCell = Excel.cell(sheet, 8, 2);
		dataCell.setCellValue(new Date());
		dataCell.setCellStyle(Excel.dateStyle(wb));
		Excel.autoSize(sheet, 1, 2);
	}

	private String product(ProductSystem system) {
		Exchange e = system.getReferenceExchange();
		if (e == null || e.getFlow() == null)
			return "";
		return e.getFlow().getName();
	}

	private String amount(ProductSystem system) {
		if (system.getTargetUnit() == null)
			return "";
		return system.getTargetAmount() + " " + system.getTargetUnit().getName();
	}

	private String method(CalculationSetup setup) {
		if (setup.impactMethod == null)
			return "none";
		else
			return setup.impactMethod.getName();
	}

	private String nwset(CalculationSetup setup) {
		if (setup.nwSet == null)
			return "none";
		else
			return setup.nwSet.getName();
	}

	private String allocation(CalculationSetup setup) {
		AllocationMethod method = setup.allocationMethod;
		if (method == null)
			return "none";
		switch (method) {
		case CAUSAL:
			return "causal";
		case ECONOMIC:
			return "economic";
		case NONE:
			return "none";
		case PHYSICAL:
			return "physical";
		case USE_DEFAULT:
			return "process defaults";
		default:
			return "unknown";
		}
	}

	private void row(int row, String header) {
		Excel.cell(sheet, row, 1, header).setCellStyle(headerStyle);
	}

	private void row(int row, String header, String value) {
		Excel.cell(sheet, row, 1, header).setCellStyle(headerStyle);
		if (value != null)
			Excel.cell(sheet, row, 2, value);
	}

}
