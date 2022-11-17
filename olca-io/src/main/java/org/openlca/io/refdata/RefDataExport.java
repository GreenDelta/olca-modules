package org.openlca.io.refdata;

import java.io.File;
import java.nio.file.Files;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefDataExport implements Runnable {

	private final IDatabase db;
	private final File dir;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ExportConfig config;

	public RefDataExport(File dir, IDatabase db) {
		this.dir = dir;
		this.db = db;
		this.config = ExportConfig.of(dir, db);
	}

	@Override
	public void run() {
		try {
			if (!dir.exists()) {
				Files.createDirectories(dir.toPath());
			}
			new LocationExport(config).run();
			new UnitGroupExport(config).run();
			new FlowPropertyExport(config).run();
			new FlowExport(config).run();
			new CurrencyExport(config).run();


			export("lcia_methods.csv", new ImpactMethodExport());
			export("lcia_categories.csv", new ImpactCategoryExport());
			export("lcia_factors.csv", new ImpactFactorExport());
			export("nw_sets.csv", new NwSetExport());
			export("nw_set_factors.csv", new NwSetFactorExport());

			exportMappingFiles();
		} catch (Exception e) {
			log.error("Reference data export failed", e);
		}
	}

	private void export(String fileName, Export export) {
		var file = new File(dir, fileName);
		if (file.exists()) {
			log.warn("the file already exists; skipped it");
		} else {
			export.run(file, db);
		}
	}

	private void exportMappingFiles() throws Exception {
		var dao = new MappingFileDao(db);
		var names = dao.getNames();
		if (names.isEmpty())
			return;

		var mapDir = new File(dir, "mappings");
		if (!mapDir.exists()) {
			Files.createDirectories(mapDir.toPath());
		}

		for (var name : names) {
			var mapFile = dao.getForName(name);
			if (mapFile == null || mapFile.content == null)
				continue;
			var file = new File(mapDir, name);
			var data = BinUtils.isGzip(mapFile.content)
					? BinUtils.gunzip(mapFile.content)
					: mapFile.content;
			Files.write(file.toPath(), data);
		}
	}
}
