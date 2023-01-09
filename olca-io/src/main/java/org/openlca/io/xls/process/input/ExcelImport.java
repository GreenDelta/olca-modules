package org.openlca.io.xls.process.input;

import java.io.File;

import org.openlca.core.database.IDatabase;

public class ExcelImport {

	private final ImportConfig config;

	public ExcelImport(IDatabase db) {
		this.config = ImportConfig.of(db);
	}

	public void next(File file) {
		var process = ProcessWorkbook.read(file, config);
		if (process == null)
			return;
		var p = process.id == 0
				? config.db().insert(process)
				: config.db().update(process);
		config.providers().pop(p);
	}

}
