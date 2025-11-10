package org.openlca.io.xls.process;

import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;

class InLocationSync {

	private final InConfig config;
	private final SheetReader sheet;

	private InLocationSync(InConfig config, SheetReader sheet) {
		this.config = config;
		this.sheet = sheet;
	}

	static void sync(InConfig config) {
		var sheet = config.getSheet(Tab.LOCATIONS);
		if (sheet == null)
			return;
		new InLocationSync(config, sheet).sync();
	}

	private void sync() {
		sheet.eachRow(row -> {
			var refId = row.str(Field.UUID);
			config.index().sync(Location.class, refId, () -> {
				var loc = new Location();
				In.mapBase(row, loc);
				loc.category = row.syncCategory(config.db(), ModelType.LOCATION);
				loc.code = row.str(Field.CODE);
				loc.latitude = row.num(Field.LATITUDE);
				loc.longitude = row.num(Field.LONGITUDE);
				return loc;
			});
		});
	}

}
