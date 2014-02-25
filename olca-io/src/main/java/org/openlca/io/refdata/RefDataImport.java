package org.openlca.io.refdata;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RefDataImport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private File dir;
	private IDatabase database;

	public RefDataImport(File dir, IDatabase database) {
		this.dir = dir;
		this.database = database;
	}

	@Override
	public void run() {
		try {
			database.getEntityFactory().getCache().evictAll();
			Seq seq = new Seq(database);
			File file = new File(dir, "categories.csv");
			if (file.exists())
				new CategoryImport().run(file, seq, database);
			database.getEntityFactory().getCache().evictAll();
		} catch (Exception e) {
			log.error("Reference data import failed", e);
		}
	}
}
