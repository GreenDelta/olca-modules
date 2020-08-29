package org.openlca.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.io.xls.Excel;

public class ExcelLimits {

	public static final int MAX_COLUMN_INDEX = SpreadsheetVersion.EXCEL2007.getLastColumnIndex();
	
	public static void main(String[] args) throws Exception {
		var wb = new XSSFWorkbook();
		var sheet = wb.createSheet("test");
		for (int i = 0; i < 1_000_000; i++) {
			Excel.cell(sheet, i, 0, 1.0);
			Excel.cell(sheet, i, i, 1.0);
			Excel.cell(sheet, 0, i, 1.0);
		}
		var file = new File("C:/Users/Win10/Desktop/rems/big.xlsx");
		try (var stream = new FileOutputStream(file);
				var buffer = new BufferedOutputStream(stream)){
			wb.write(buffer);
			wb.close();
			// wb.dispose();
		}
	}
	
}
