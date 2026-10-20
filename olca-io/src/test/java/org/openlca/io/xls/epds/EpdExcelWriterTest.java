package org.openlca.io.xls.epds;

import static org.junit.Assert.*;

import java.nio.file.Files;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;

public class EpdExcelWriterTest {

	@Test
	public void testWrite() throws Exception {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("product", mass);

		var epd = Epd.of("Example EPD", product);
		epd.refId = "epd-ref-id";

		var file = Files.createTempFile("_olca_epd_", ".xlsx").toFile();
		try {
			new EpdExcelWriter(epd, file)
				.write()
				.orElseThrow();

			try (var wb = new XSSFWorkbook(file)) {
				var sheet = wb.getSheet(Tab.EPD_INFORMATION.label());
				assertNotNull(sheet);
				check(sheet, 1, 1, "UUID");
				check(sheet, 1, 2, "epd-ref-id");
				check(sheet, 2, 1, "Name");
				check(sheet, 2, 2, "Example EPD");
			}
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	private static void check(Sheet sheet, int row, int col, String expected) {
		assertEquals(expected, sheet.getRow(row).getCell(col).getStringCellValue());
	}
}
