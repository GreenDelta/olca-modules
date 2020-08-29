package org.openlca.io.xls;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.io.xls.Excel;

public class ExcelLimitsTest {

	/**
	 * Tests that we can exceed the column limit without
	 * an exception. We ignore this test by default because
	 * it takes some time to run it.
	 */
	@Test
	@Ignore
	public void testLimits() throws Exception {
		var wb = new SXSSFWorkbook(-1);
		var sheet = wb.createSheet("test");
		for (int i = 0; i < 1_000_000; i++) {
			Excel.cell(sheet, i, 0, 1.0);
			Excel.cell(sheet, i, i, 1.0);
			Excel.cell(sheet, 0, i, 1.0);
		}
		var file = Files.createTempFile("_olca_test_", ".xlsx").toFile();
		try (var stream = new FileOutputStream(file);
				var buffer = new BufferedOutputStream(stream)){
			wb.write(buffer);
			wb.close();
			wb.dispose();
		}
		Assert.assertTrue(file.length() > 0);
		Files.delete(file.toPath());
	}

}
