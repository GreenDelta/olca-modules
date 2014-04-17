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

	public static void read(final Config config) {
		new ActorSheet(config).read();
	}

	private final Config config;
	private final ActorDao dao;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Sheet sheet;

	private ActorSheet(final Config config) {
		this.config = config;
		this.dao = new ActorDao(config.database);
		sheet = config.workbook.getSheet("Actors");
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("import actors");
			int row = 1;
			while (true) {
				final String uuid = config.getString(sheet, row, 0);
				if (uuid == null || uuid.trim().isEmpty()) {
					break;
				}
				readActor(uuid, row);
				row++;
			}
		} catch (final Exception e) {
			log.error("failed to read actor sheet", e);
		}
	}

	private void readActor(final String uuid, final int row) {
		final String name = config.getString(sheet, row, 1);
		final String category = config.getString(sheet, row, 3);
		Actor actor = dao.getForRefId(uuid);
		if (actor != null) {
			config.refData.putActor(name, category, actor);
			return;
		}
		actor = new Actor();
		actor.setRefId(uuid);
		actor.setName(name);
		actor.setDescription(config.getString(sheet, row, 2));
		actor.setCategory(config.getCategory(category, ModelType.ACTOR));
		setAttributes(row, actor);
		actor = dao.insert(actor);
		config.refData.putActor(name, category, actor);
	}

	private void setAttributes(final int row, final Actor actor) {
		final String version = config.getString(sheet, row, 4);
		actor.setVersion(Version.fromString(version).getValue());
		final Date lastChange = config.getDate(sheet, row, 5);
		if (lastChange != null) {
			actor.setLastChange(lastChange.getTime());
		}
		actor.setAddress(config.getString(sheet, row, 6));
		actor.setCity(config.getString(sheet, row, 7));
		actor.setZipCode(config.getString(sheet, row, 8));
		actor.setCountry(config.getString(sheet, row, 9));
		actor.setEmail(config.getString(sheet, row, 10));
		actor.setTelefax(config.getString(sheet, row, 11));
		actor.setTelephone(config.getString(sheet, row, 12));
		actor.setWebsite(config.getString(sheet, row, 13));
	}

}
