package org.openlca.io.xls.process.input;

import java.io.File;

import org.junit.Test;
import org.openlca.io.xls.process.Field;
import org.openlca.io.xls.process.Tab;

public class ReaderTest {

	@Test
	public void testReadSection() {
		var wb = WorkbookReader.open(
						new File("4db11058-10a7-32d1-8d0d-6dd82911e0ba_03.10.003.xlsx"))
				.orElseThrow();
		var sheet = wb.getSheet(Tab.OUTPUTS).orElseThrow();
		sheet.eachRow((fields, row) -> {
			fields.value(row, Field.IS)
		});
	}
}
