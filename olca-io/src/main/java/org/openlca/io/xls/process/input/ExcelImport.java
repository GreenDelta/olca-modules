package org.openlca.io.xls.process.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.io.ImportLog;

public class ExcelImport {

	private final IDatabase db;
	private final ImportLog log;
	private final ExchangeProviderQueue providers;

	public ExcelImport(IDatabase db) {
		this.db = db;
		this.log = new ImportLog();
		this.providers = ExchangeProviderQueue.create(db);
	}

	IDatabase db() {
		return db;
	}

	ImportLog log() {
		return log;
	}

	public void next(File file) {
		var process = ProcessWorkbook.read(file, this);
		if (process == null)
			return;
		var p = process.id == 0
				? db.insert(process)
				: db.update(process);
		providers.pop(p);
	}

}
