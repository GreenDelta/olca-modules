package org.openlca.io.refdata;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefDataImport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File dir;
	private final IDatabase database;
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

	/**
	 * Mapping files are *.csv files that are located in a sub-folder mappings
	 * of the import directory.
	 */
	private void importMappingFiles() throws Exception {

		var mapDir = new File(dir, "mappings");
		if (!mapDir.exists())
			return;
		var files = mapDir.listFiles();
		if (files == null)
			return;

		var dao = new MappingFileDao(database);
		var existing = dao.getNames().stream()
				.map(String::toLowerCase)
				.collect(Collectors.toSet());

		for (var file : files) {
			if (!file.isFile())
				continue;
			var name = file.getName().toLowerCase();
			if (!name.endsWith(".csv"))
				continue;
			if (existing.contains(name)) {
				log.warn("A mapping file {} already exists" +
						" in the database and was not imported again", file);
				continue;
			}

			var data = Files.readAllBytes(file.toPath());
			var mapFile = new MappingFile();
			mapFile.content = BinUtils.gzip(data);
			mapFile.name = file.getName();
			dao.insert(mapFile);
		}
	}

}
