package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.io.xls.process.Field;

class Actors {

	private final ProcessWorkbook wb;

	private Actors(ProcessWorkbook wb) {
		this.wb = wb;
	}

	static void sync(ProcessWorkbook wb) {
		new Actors(wb).sync();
	}

	private void sync() {
		var sheet = wb.getSheet("Actors");
		if (sheet == null)
			return;
		var fields = FieldMap.parse(sheet.getRow(0));
		if (fields.isEmpty())
			return;
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var refId = fields.str(row, Field.UUID);
			wb.index.sync(Actor.class, refId, () -> create(row, fields));
		});
	}

	private Actor create(Row row, FieldMap fields) {
		var actor = new Actor();
		Util.mapBase(row, fields, actor);
		actor.category = fields.category(row, ModelType.ACTOR, wb.db);
		actor.address = fields.str(row, Field.ADDRESS);
		actor.city = fields.str(row, Field.CITY);
		actor.zipCode = fields.str(row, Field.ZIP_CODE);
		actor.country = fields.str(row, Field.COUNTRY);
		actor.email = fields.str(row, Field.E_MAIL);
		actor.telefax = fields.str(row, Field.TELEFAX);
		actor.telephone = fields.str(row, Field.TELEPHONE);
		actor.website = fields.str(row, Field.WEBSITE);
		return actor;
	}
}
