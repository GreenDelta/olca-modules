package org.openlca.shell;

import java.io.File;

import org.openlca.core.database.DatabaseContent;
import org.openlca.core.database.derby.DerbyDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerbyCommand {

	private final int EMPTY = 0;
	private final int UNITS = 1;
	private final int FULL = 2;

	private Logger log = LoggerFactory.getLogger(getClass());

	public void exec(Shell shell, String[] args) {
		if (args.length < 1 || args[0] == null)
			log.error("too little arguments");
		File directory = new File(args[0]);
		int type = fetchType(args);
		runIt(directory, shell, type);
	}

	private int fetchType(String[] args) {
		if (args == null || args.length < 2 || args[1] == null)
			return EMPTY;
		String typeStr = args[1].trim().toLowerCase();
		switch (typeStr) {
		case "units":
			return UNITS;
		case "full":
			return FULL;
		default:
			log.error("unknown database type: {}", typeStr);
			return EMPTY;
		}
	}

	private void runIt(File directory, Shell shell, int type) {
		boolean isNew = !directory.exists();
		if (isNew)
			createNew(directory, shell, type);
		else
			connectToExisting(directory, shell);
	}

	private void connectToExisting(File directory, Shell shell) {
		log.info("connect to existing database {}", directory);
		try {
			DerbyDatabase db = new DerbyDatabase(directory);
			shell.setDatabase(db);
			log.info("connection established");
		} catch (Exception e) {
			log.error("failed to connect to database", e);
		}
	}

	private void createNew(File directory, Shell shell, int type) {
		log.info("create new database {}", directory);
		try {
			if (!directory.getParentFile().exists())
				directory.getParentFile().mkdirs();
			DerbyDatabase db = new DerbyDatabase(directory);
			if (type == UNITS) {
				log.info("fill database with reference units");
				db.fill(DatabaseContent.UNITS);
			} else if (type == FULL) {
				log.info("fill database with all reference data");
				db.fill(DatabaseContent.ALL_REF_DATA);
			}
			shell.setDatabase(db);
			log.info("connection established");
		} catch (Exception e) {
			log.error("failed to create database", e);
		}
	}

}
