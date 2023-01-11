package org.openlca.io.xls.process.input;

import java.io.File;

import org.apache.poi.ss.usermodel.Row;
import org.junit.Test;
import org.openlca.io.xls.process.Field;
import org.openlca.io.xls.process.Tab;

public class ReaderTest {

	@Test
	public void testReadSection() {
		var wb = WorkbookReader.open(
						new File("../target/4db11058-10a7-32d1-8d0d-6dd82911e0ba_03.10.003.xlsx"))
				.orElseThrow();
		var sheet = wb.getSheet(Tab.OUTPUTS).orElseThrow();
		sheet.eachRow((fields, row) -> {
			if (isRefRow(fields, row)) {
				var flow = fields.value(row, Field.FLOW);
				System.out.println("ref. flow: " + flow);
			}
		});
	}

	private boolean isRefRow(FieldMap fields, Row row) {
		var val = fields.value(row, Field.IS_REFERENCE);
		if (val == null)
			return false;
		if (val instanceof String s)
			return !s.isBlank();
		if (val instanceof Boolean b)
			return b;
		if (val instanceof Number n)
			return n.doubleValue() > 0;
		return false;
	}
}
