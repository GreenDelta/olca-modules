package org.openlca.io.refdata;

import java.io.File;
import java.nio.file.Files;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefDataExport implements Runnable {

	private final IDatabase database;
	private final File dir;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public RefDataExport(File dir, IDatabase database) {
		this.dir = dir;
		this.database = database;
	}

	@Override
	public void run() {
		try {
			if (!dir.exists()) {
				Files.createDirectories(dir.toPath());
			}
			export("locations.csv", new LocationExport());
			export("categories.csv", new CategoryExport());
			export("units.csv", new UnitExport());
			export("unit_groups.csv", new UnitGroupExport());
			export("flow_properties.csv", new FlowPropertyExport());
			export("flows.csv", new FlowExport());
			export("flow_property_factors.csv", new FlowPropertyFactorExport());
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

	private void export(String fileName, AbstractExport export) {
		var file = new File(dir, fileName);
		if (file.exists()) {
			log.warn("the file already exists; did not changed it");
		} else {
			export.run(file, database);
		}
	}

	private void exportMappingFiles() throws Exception {
		var dao = new MappingFileDao(database);
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
