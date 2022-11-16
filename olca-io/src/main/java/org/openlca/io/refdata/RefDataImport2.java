package org.openlca.io.refdata;

import java.io.File;

import org.openlca.core.database.IDatabase;

public class RefDataImport2 implements Runnable {

	private final ImportConfig config;

	public RefDataImport2(File dir, IDatabase db) {
		this.config = ImportConfig.of(dir, db);
	}

	@Override
	public void run() {
		new UnitGroupImport2(config).run();
		new FlowPropertyImport2(config).run();
	}
}
