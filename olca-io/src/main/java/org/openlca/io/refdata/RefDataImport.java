package org.openlca.io.refdata;

import java.io.File;

import org.openlca.core.database.IDatabase;

public class RefDataImport implements Runnable {

	private final ImportConfig config;

	public RefDataImport(File dir, IDatabase db) {
		this.config = ImportConfig.of(dir, db);
	}

	@Override
	public void run() {
		new UnitGroupImport(config).run();
		new FlowPropertyImport(config).run();
		new FlowImport(config).run();
		new CurrencyImport(config).run();
		new LocationImport(config).run();
	}
}
