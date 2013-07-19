package org.openlca.shell;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.io.UnitMapping;
import org.openlca.io.ecospold1.importer.EcoSpold01Import;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final int UNKNOWN = -1;
	private final int ECOSPOLD_1 = 0;

	public void exec(Shell shell, String[] args) {
		if (args.length < 2) {
			log.error("to little arguments");
			return;
		}
		IDatabase database = shell.getDatabase();
		if (database == null) {
			log.error("no database connection");
			return;
		}
		int format = fetchFormat(args);
		File file = fetchFile(args);
		if (format == UNKNOWN || file == null || !file.exists()) {
			fail();
			return;
		}
		runImport(format, file, database);
	}

	private void runImport(int format, File file, IDatabase database) {
		switch (format) {
		case ECOSPOLD_1:
			importEcoSpold01(file, database);
			break;
		default:
			break;
		}
	}

	private void importEcoSpold01(File file, IDatabase database) {
		log.info("import EcoSpold 01 data sets from {}", file);
		try {
			UnitMapping mapping = UnitMapping.createDefault(database);
			EcoSpold01Import es1Import = new EcoSpold01Import(database, mapping);
			if (file.isDirectory())
				es1Import.run(file.listFiles());
			else
				es1Import.run(file);
			es1Import.close();
			log.info("import done");
		} catch (Exception e) {
			log.error("failed to import data sets", e);
		}
	}

	private int fetchFormat(String[] args) {
		String formatStr = args[0];
		if (formatStr == null)
			return -1;
		formatStr = formatStr.trim().toLowerCase();
		switch (formatStr) {
		case "ecospold_1":
			return ECOSPOLD_1;
		default:
			return UNKNOWN;
		}
	}

	private File fetchFile(String[] args) {
		String filePath = args[1];
		if (filePath == null)
			return null;
		return new File(filePath);
	}

	private void fail() {
		log.error("unknown import format or not an expected file.");
	}

}
