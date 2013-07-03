package org.openlca.io.ecospold2;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessExport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Process process;
	private IDatabase database;

	public ProcessExport(Process process, IDatabase database) {
		this.process = process;
		this.database = database;
	}

	public void run(File targetDir) {
		log.debug("Export process {} to directory {}", process, targetDir);
		File outFile = new File(targetDir, process.getRefId().concat(".spold"));

	}

}
