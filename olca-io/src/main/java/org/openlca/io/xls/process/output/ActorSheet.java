package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Actor;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

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
		var sheet = wb.createSheet("Actors");
		Excel.trackSize(sheet, 0, 5);
		writeHeader(sheet);
		int row = 0;
		for (var actor : Util.sort(actors)) {
			row++;
			write(sheet, row, actor);
		}
		Excel.autoSize(sheet, 0, 5);
	}

	private void writeHeader(Sheet sheet) {
		wb.header(sheet, 0, 0, "UUID");
		wb.header(sheet, 0, 1, "Name");
		wb.header(sheet, 0, 2, "Description");
		wb.header(sheet, 0, 3, "Category");
		wb.header(sheet, 0, 4, "Version");
		wb.header(sheet, 0, 5, "Last change");
		wb.header(sheet, 0, 6, "Address");
		wb.header(sheet, 0, 7, "City");
		wb.header(sheet, 0, 8, "Zip code");
		wb.header(sheet, 0, 9, "Country");
		wb.header(sheet, 0, 10, "E-mail");
		wb.header(sheet, 0, 11, "Telefax");
		wb.header(sheet, 0, 12, "Telephone");
		wb.header(sheet, 0, 13, "Website");
	}

	private void write(Sheet sheet, int row, Actor actor) {
		Excel.cell(sheet, row, 0, actor.refId);
		Excel.cell(sheet, row, 1, actor.name);
		Excel.cell(sheet, row, 2, actor.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(actor.category));
		Excel.cell(sheet, row, 4, Version.asString(actor.version));
		wb.date(sheet, row, 5, actor.lastChange);
		Excel.cell(sheet, row, 6, actor.address);
		Excel.cell(sheet, row, 7, actor.city);
		Excel.cell(sheet, row, 8, actor.zipCode);
		Excel.cell(sheet, row, 9, actor.country);
		Excel.cell(sheet, row, 10, actor.email);
		Excel.cell(sheet, row, 11, actor.telefax);
		Excel.cell(sheet, row, 12, actor.telephone);
		Excel.cell(sheet, row, 13, actor.website);
	}
}
