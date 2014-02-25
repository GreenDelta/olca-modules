package org.openlca.io.refdata;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefDataExport implements Runnable {

	private IDatabase database;
	private File dir;
	private Logger log = LoggerFactory.getLogger(getClass());

	public RefDataExport(File dir, IDatabase database) {
		this.dir = dir;
		this.database = database;
	}

	@Override
	public void run() {
		if (!dir.exists())
			dir.mkdirs();
		File categoryFile = new File(dir, "categories.csv");
		if (categoryFile.exists()) {
			log.warn("the category file already exists; did not changed it");
		} else {
			new CategoryExport(categoryFile, database).run();
		}
	}
}
