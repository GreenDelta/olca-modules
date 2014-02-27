package org.openlca.io.refdata;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
		try {
			if (!dir.exists())
				dir.mkdirs();
			export("locations.csv", new LocationExport());
			export("categories.csv", new CategoryExport());
			export("units.csv", new UnitExport());
			export("unit_groups.csv", new UnitGroupExport());
			export("flow_properties.csv", new FlowPropertyExport());
		} catch (Exception e) {
			log.error("Reference data export failed", e);
		}
	}

	private void export(String fileName, Export export) {
		File file = new File(dir, fileName);
		if (file.exists()) {
			log.warn("the file already exists; did not changed it");
		} else {
			export.run(file, database);
		}
	}
}
