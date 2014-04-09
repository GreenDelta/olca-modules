package org.openlca.io.refdata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.openlca.io.maps.Maps;
import org.openlca.util.BinUtils;
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
		try {
			if (!dir.exists())
				dir.mkdirs();
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
		File file = new File(dir, fileName);
		if (file.exists()) {
			log.warn("the file already exists; did not changed it");
		} else {
			export.run(file, database);
		}
	}

	private void exportMappingFiles() throws Exception {
		MappingFileDao dao = new MappingFileDao(database);
		// TODO: add other mapping files
		String[] fileNames = { Maps.SP_FLOW_IMPORT, Maps.ES2_UNIT_EXPORT,
				Maps.ES2_LOCATION_EXPORT, Maps.ES2_COMPARTMENT_EXPORT,
				Maps.ES2_FLOW_EXPORT };
		for (String fileName : fileNames) {
			File file = new File(dir, fileName);
			FileOutputStream out = new FileOutputStream(file);
			MappingFile mappingFile = dao.getForFileName(fileName);
			InputStream in;
			if (mappingFile != null && mappingFile.getContent() != null) {
				byte[] bytes = BinUtils.unzip(mappingFile.getContent());
				in = new ByteArrayInputStream(bytes);
			} else {
				in = Maps.class.getResourceAsStream(fileName);
			}
			IOUtils.copy(in, out);
		}
	}
}
