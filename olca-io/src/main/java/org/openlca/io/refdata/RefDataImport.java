package org.openlca.io.refdata;

import java.io.File;
import java.io.FileInputStream;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			importFile("flow_property_factors.csv",
					new FlowPropertyFactorImport());
			importFile("currencies.csv", new CurrencyImport());
			importFile("lcia_methods.csv", new ImpactMethodImport());
			importFile("lcia_categories.csv", new ImpactCategoryImport());
			importFile("lcia_factors.csv", new ImpactFactorImport());
			importFile("nw_sets.csv", new NwSetImport());
			importFile("nw_set_factors.csv", new NwSetFactorImport());
			seq.write();
			database.getEntityFactory().getCache().evictAll();
			importMappingFiles();
			importKmlFile();
		} catch (Exception e) {
			log.error("Reference data import failed", e);
		}
	}

	private void importFile(String fileName, AbstractImport importer)
			throws Exception {
		File file = new File(dir, fileName);
		if (!file.exists()) {
			log.info("file {} does not exist in {} -> not imported", fileName,
					dir);
			return;
		}
		log.info("import file {}", file);
		importer.run(file, seq, database);
	}

	private void importMappingFiles() throws Exception {
		String[] fileNames = {
				Maps.ES1_FLOW_IMPORT,
				Maps.ES2_FLOW_IMPORT,
				Maps.ILCD_FLOW_IMPORT,
				Maps.SP_FLOW_IMPORT,
				Maps.ES2_UNIT_EXPORT,
				Maps.ES2_LOCATION_EXPORT,
				Maps.ES2_COMPARTMENT_EXPORT,
				Maps.ES2_FLOW_EXPORT,
				Maps.LOCATION_IMPORT };
		for (String fileName : fileNames) {
			File file = new File(dir, fileName);
			if (!file.exists())
				continue;
			try (FileInputStream stream = new FileInputStream(file)) {
				Maps.store(fileName, stream, database);
			}
		}
	}

	private void importKmlFile() {
		File kmlFile = new File(dir, "Geographies.xml");
		if (!kmlFile.exists()) {
			log.trace("{} does not exist; no KML import", kmlFile);
			return;
		}
		log.trace("import KML data from {}", kmlFile);
		new GeoKmzImport(kmlFile, database).run();
	}
}
