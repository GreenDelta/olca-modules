package org.openlca.io.refdata;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RefDataImport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private File dir;
	private IDatabase database;
	private Seq seq;

	public RefDataImport(File dir, IDatabase database) {
		this.dir = dir;
		this.database = database;
	}

	@Override
	public void run() {
		try {
			database.getEntityFactory().getCache().evictAll();
			seq = new Seq(database);
			importFile("locations.csv", new LocationImport());
			importFile("categories.csv", new CategoryImport());
			importFile("units.csv", new UnitImport());
			importFile("unit_groups.csv", new UnitGroupImport());
			importFile("flow_properties.csv", new FlowPropertyImport());
			importFile("flows.csv", new FlowImport());
			importFile("flow_property_factors.csv", new FlowPropertyFactorImport());
			database.getEntityFactory().getCache().evictAll();
		} catch (Exception e) {
			log.error("Reference data import failed", e);
		}
	}

	private void importFile(String fileName, Import importer) throws Exception {
		File file = new File(dir, fileName);
		if (!file.exists()) {
			log.info("file {} does not exist in {} -> not imported", fileName,
					dir);
			return;
		}
		log.info("import file {}", file);
		importer.run(file, seq, database);
	}

}
