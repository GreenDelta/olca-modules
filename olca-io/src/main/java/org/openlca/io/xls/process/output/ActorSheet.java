package org.openlca.io.xls.process.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;

import java.util.HashSet;
import java.util.Set;

class ActorSheet implements EntitySheet {

	private final ProcessWorkbook wb;
	private final Set<Actor> actors = new HashSet<>();

	ActorSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Actor actor) {
			actors.add(actor);
		}
	}

	@Override
	public void flush() {
		if (actors.isEmpty())
			return;
		var cursor = wb.createCursor("Actors")
				.withColumnWidths(5, 40);

		cursor.header(
				Field.UUID,
				Field.NAME,
				Field.DESCRIPTION,
				Field.CATEGORY,
				Field.VERSION,
				Field.LAST_CHANGE,
				Field.ADDRESS,
				Field.CITY,
				Field.ZIP_CODE,
				Field.COUNTRY,
				Field.E_MAIL,
				Field.TELEFAX,
				Field.TELEPHONE,
				Field.WEBSITE);

		for (var actor : Util.sort(actors)) {
			cursor.next(row -> {
				Excel.cell(row, 0, actor.refId);
				Excel.cell(row, 1, actor.name);
				Excel.cell(row, 2, actor.description);
				Excel.cell(row, 3, CategoryPath.getFull(actor.category));
				Excel.cell(row, 4, Version.asString(actor.version));
				wb.date(row, 5, actor.lastChange);
				Excel.cell(row, 6, actor.address);
				Excel.cell(row, 7, actor.city);
				Excel.cell(row, 8, actor.zipCode);
				Excel.cell(row, 9, actor.country);
				Excel.cell(row, 10, actor.email);
				Excel.cell(row, 11, actor.telefax);
				Excel.cell(row, 12, actor.telephone);
				Excel.cell(row, 13, actor.website);
			});
		}
	}

}
