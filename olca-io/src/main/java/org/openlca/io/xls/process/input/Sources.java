package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.io.xls.process.Field;

class Sources {

	private final ProcessWorkbook wb;

	private Sources(ProcessWorkbook wb) {
		this.wb = wb;
	}

	public static void sync(ProcessWorkbook config) {
		new Sources(config).sync();
	}

	private void sync() {
		var sheet = wb.getSheet("Sources");
		if (sheet == null)
			return;
		var fields = FieldMap.parse(sheet.getRow(0));
		if (fields.isEmpty())
			return;
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var refId = fields.str(row, Field.UUID);
			wb.index.sync(Source.class, refId, () -> create(row, fields));
		});
	}

	private Source create(Row row, FieldMap fields) {
		var source = new Source();
		Util.mapBase(row, fields, source);
		source.category = fields.category(row, ModelType.SOURCE, wb.db);
		source.url = fields.str(row, Field.URL);
		source.textReference = fields.str(row, Field.TEXT_REFERENCE);
		var year = fields.num(row, Field.YEAR);
		if (year > 0) {
			source.year = (short) year;
		}
		return source;
	}
}
