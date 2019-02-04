package org.openlca.io.xls.process.input;

import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ActorSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final ActorDao dao;
	private final Sheet sheet;

	private ActorSheet(Config config) {
		this.config = config;
		this.dao = new ActorDao(config.database);
		sheet = config.workbook.getSheet("Actors");
	}

	public static void read(Config config) {
		new ActorSheet(config).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("import actors");
			int row = 1;
			while (true) {
				String uuid = config.getString(sheet, row, 0);
				if (uuid == null || uuid.trim().isEmpty()) {
					break;
				}
				readActor(uuid, row);
				row++;
			}
		} catch (Exception e) {
			log.error("failed to read actor sheet", e);
		}
	}

	private void readActor(String uuid, int row) {
		String name = config.getString(sheet, row, 1);
		String category = config.getString(sheet, row, 3);
		Actor actor = dao.getForRefId(uuid);
		if (actor != null) {
			config.refData.putActor(name, category, actor);
			return;
		}
		actor = new Actor();
		actor.refId = uuid;
		actor.name = name;
		actor.description = config.getString(sheet, row, 2);
		actor.category = config.getCategory(category, ModelType.ACTOR);
		setAttributes(row, actor);
		actor = dao.insert(actor);
		config.refData.putActor(name, category, actor);
	}

	private void setAttributes(int row, Actor actor) {
		String version = config.getString(sheet, row, 4);
		actor.version = Version.fromString(version).getValue();
		Date lastChange = config.getDate(sheet, row, 5);
		if (lastChange != null) {
			actor.lastChange = lastChange.getTime();
		}
		actor.address = config.getString(sheet, row, 6);
		actor.city = config.getString(sheet, row, 7);
		actor.zipCode = config.getString(sheet, row, 8);
		actor.country = config.getString(sheet, row, 9);
		actor.email = config.getString(sheet, row, 10);
		actor.telefax = config.getString(sheet, row, 11);
		actor.telephone = config.getString(sheet, row, 12);
		actor.website = config.getString(sheet, row, 13);
	}

}
