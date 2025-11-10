package org.openlca.io.xls.process;

class InProviderSync {

	private final InConfig config;

	InProviderSync(InConfig config) {
		this.config = config;
	}

	static void sync(InConfig config) {
		new InProviderSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.PROVIDERS);
		if (sheet == null)
			return;
		sheet.eachRow(row -> {
			var name = row.str(Field.NAME);
			var refId = row.str(Field.UUID);
			config.index().addProvider(name, refId);
		});
	}

}
