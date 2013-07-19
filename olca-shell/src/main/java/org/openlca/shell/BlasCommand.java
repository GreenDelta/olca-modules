package org.openlca.shell;

import java.io.File;

import org.openlca.jblas.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlasCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	public void exec(String[] args) {
		if (args.length < 1 || args[0] == null) {
			log.error("the blas command takes a directory as argument");
		}
		try {
			log.info("load native BLAS library");
			String path = args[0];
			File dir = new File(path);
			Library.loadFromDir(dir);
		} catch (Exception e) {
			log.error("failed to load the BLAS library", e);
		}
	}

}
