package org.openlca.io.xls.epds;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

record Styles(
	CellStyle bold,
	CellStyle date,
	CellStyle dateTime,
	CellStyle plain,
	CellStyle wrap
) {

	static Styles of(Workbook wb) {
		var boldFont = wb.createFont();
		boldFont.setBold(true);
		var bold = wb.createCellStyle();
		bold.setFont(boldFont);

		var date = wb.createCellStyle();
		date.setDataFormat(wb.createDataFormat().getFormat("mm/dd/yyyy"));
		date.setAlignment(HorizontalAlignment.LEFT);

		var dateTime = wb.createCellStyle();
		dateTime.setDataFormat(wb.createDataFormat().getFormat("mm/dd/yyyy hh:mm"));
		dateTime.setAlignment(HorizontalAlignment.LEFT);

		var plain = wb.createCellStyle();
		plain.setVerticalAlignment(VerticalAlignment.TOP);

		var wrap = wb.createCellStyle();
		wrap.setWrapText(true);
		wrap.setVerticalAlignment(VerticalAlignment.TOP);

		return new Styles(bold, date, dateTime, plain, wrap);
	}
}
