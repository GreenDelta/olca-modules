package org.openlca.io.xls.process.input;

import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;

class ActorSheet {

	private final ProcessWorkbook wb;

	private ActorSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	public static void sync(ProcessWorkbook config) {
		new ActorSheet(config).sync();
	}

	private void sync() {
		var sheet = wb.getSheet("Actors");
		if (sheet == null)
			return;

		int row = 1;
		while (true) {
			String uuid = wb.getString(sheet, row, 0);
			if (uuid == null || uuid.trim().isEmpty()) {
				break;
			}
			readActor(uuid, row);
			row++;
		}
	}

	private void readActor(String uuid, int row) {
		String name = wb.getString(sheet, row, 1);
		String category = wb.getString(sheet, row, 3);
		Actor actor = dao.getForRefId(uuid);
		if (actor != null) {
			wb.refData.putActor(name, category, actor);
			return;
		}
		actor = new Actor();
		actor.refId = uuid;
		actor.name = name;
		actor.description = wb.getString(sheet, row, 2);
		actor.category = wb.getCategory(category, ModelType.ACTOR);
		setAttributes(row, actor);
		actor = dao.insert(actor);
		wb.refData.putActor(name, category, actor);
	}

	private void setAttributes(int row, Actor actor) {
		String version = wb.getString(sheet, row, 4);
		actor.version = Version.fromString(version).getValue();
		Date lastChange = wb.getDate(sheet, row, 5);
		if (lastChange != null) {
			actor.lastChange = lastChange.getTime();
		}
		actor.address = wb.getString(sheet, row, 6);
		actor.city = wb.getString(sheet, row, 7);
		actor.zipCode = wb.getString(sheet, row, 8);
		actor.country = wb.getString(sheet, row, 9);
		actor.email = wb.getString(sheet, row, 10);
		actor.telefax = wb.getString(sheet, row, 11);
		actor.telephone = wb.getString(sheet, row, 12);
		actor.website = wb.getString(sheet, row, 13);
	}

}
