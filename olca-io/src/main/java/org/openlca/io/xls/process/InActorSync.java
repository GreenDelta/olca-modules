package org.openlca.io.xls.process;

import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;

class InActorSync {

	private final InConfig config;

	private InActorSync(InConfig config) {
		this.config = config;
	}

	static void sync(InConfig config) {
		new InActorSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.ACTORS);
		if (sheet == null)
			return;
		sheet.eachRow(row -> {
			var refId = row.str(Field.UUID);
			config.index().sync(Actor.class, refId, () -> create(row));
		});
	}

	private Actor create(RowReader row) {
		var actor = new Actor();
		In.mapBase(row, actor);
		actor.category = row.syncCategory(config.db(), ModelType.ACTOR);
		actor.address = row.str(Field.ADDRESS);
		actor.city = row.str(Field.CITY);
		actor.zipCode = row.str(Field.ZIP_CODE);
		actor.country = row.str(Field.COUNTRY);
		actor.email = row.str(Field.E_MAIL);
		actor.telefax = row.str(Field.TELEFAX);
		actor.telephone = row.str(Field.TELEPHONE);
		actor.website = row.str(Field.WEBSITE);
		return actor;
	}
}
