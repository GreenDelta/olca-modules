package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class ActorSheet {

	private final Config config;
	private final Sheet sheet;
	private int row = 0;

	private ActorSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Actors");
	}

	public static void write(Config config) {
		new ActorSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 5);
		writeHeader();
		var actors = new ActorDao(config.database).getAll();
		actors.sort(new EntitySorter());
		for (Actor actor : actors) {
			row++;
			write(actor);
		}
		Excel.autoSize(sheet, 0, 5);
	}

	private void writeHeader() {
		config.header(sheet, row, 0, "UUID");
		config.header(sheet, row, 1, "Name");
		config.header(sheet, row, 2, "Description");
		config.header(sheet, row, 3, "Category");
		config.header(sheet, row, 4, "Version");
		config.header(sheet, row, 5, "Last change");
		config.header(sheet, row, 6, "Address");
		config.header(sheet, row, 7, "City");
		config.header(sheet, row, 8, "Zip code");
		config.header(sheet, row, 9, "Country");
		config.header(sheet, row, 10, "E-mail");
		config.header(sheet, row, 11, "Telefax");
		config.header(sheet, row, 12, "Telephone");
		config.header(sheet, row, 13, "Website");
	}

	private void write(Actor actor) {
		Excel.cell(sheet, row, 0, actor.refId);
		Excel.cell(sheet, row, 1, actor.name);
		Excel.cell(sheet, row, 2, actor.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(actor.category));
		Excel.cell(sheet, row, 4, Version.asString(actor.version));
		config.date(sheet, row, 5, actor.lastChange);
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
