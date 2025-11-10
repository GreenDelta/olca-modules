package org.openlca.io.xls.process;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

record Styles(
	CellStyle bold,
	CellStyle date,
	CellStyle pairLabel,
	CellStyle pairValue
) {

	static Styles of(Workbook wb) {
		var boldFont = wb.createFont();
		boldFont.setBold(true);
		var bold = wb.createCellStyle();
		bold.setFont(boldFont);

		var date = wb.createCellStyle();
		var dateFmt = wb.createDataFormat();
		date.setDataFormat(dateFmt.getFormat("mm/dd/yyyy hh:mm"));
		date.setAlignment(HorizontalAlignment.LEFT);

		var pairLabel = wb.createCellStyle();
		pairLabel.setVerticalAlignment(VerticalAlignment.TOP);
		var pairValue = wb.createCellStyle();
		pairValue.setWrapText(true);

		return new Styles(bold, date, pairLabel, pairValue);
	}

}
