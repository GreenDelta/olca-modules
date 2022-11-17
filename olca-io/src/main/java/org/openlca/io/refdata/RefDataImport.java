package org.openlca.io.refdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.MappingFile;
import org.openlca.util.BinUtils;

public class RefDataImport implements Runnable {

	private final ImportConfig config;

	public RefDataImport(File dir, IDatabase db) {
		this.config = ImportConfig.of(dir, db);
	}

	public ImportLog log() {
		return config.log();
	}

	@Override
	public void run() {
		try {
			new UnitGroupImport(config).run();
			new FlowPropertyImport(config).run();
			new FlowImport(config).run();
			new CurrencyImport(config).run();
			new LocationImport(config).run();
			new ImpactCategoryImport(config).run();
			new ImpactMethodImport(config).run();
			importMappingFiles();
		} catch (Exception e) {
			config.log().error("ref. data import failed", e);
		}
	}

	/**
	 * Mapping files are *.csv files that are located in a sub-folder mappings
	 * of the import directory.
	 */
	private void importMappingFiles() throws IOException {

		var mappingDir = new File(config.dir(), "mappings");
		if (!mappingDir.exists())
			return;
		var files = mappingDir.listFiles();
		if (files == null)
			return;

		var dao = new MappingFileDao(config.db());
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
				config.log().warn("A mapping file with this name already exists" +
						" in the database and was not imported again: " + file);
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
