package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.io.xls.process.Field;
import org.openlca.util.Strings;

import java.util.UUID;

class Util {

	static void mapBase(Row row, FieldMap fields, RefEntity e) {
		if (row == null || fields == null || e == null)
			return;
		e.refId = fields.str(row, Field.UUID);
		if (Strings.nullOrEmpty(e.refId)) {
			e.refId = UUID.randomUUID().toString();
		}
		e.name = fields.str(row, Field.NAME);
		e.description = fields.str(row, Field.DESCRIPTION);
		if (e instanceof RootEntity root) {
			//var path = fields.str(row, Field.CATEGORY);
			//var type = ModelType.of(
			var version = fields.str(row, Field.VERSION);
			root.version = Version.fromString(version).getValue();
			root.tags = fields.str(row, Field.TAGS);
			var lastChange = fields.date(row, Field.LAST_CHANGE);
			if (lastChange != null) {
				root.lastChange = lastChange.getTime();
			}
		}
	}

}
