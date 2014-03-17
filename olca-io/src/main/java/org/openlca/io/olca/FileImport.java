package org.openlca.io.olca;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * Copies the files from the file storage of the source database to the file
 * storage of the destination database.
 */
class FileImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IDatabase source;
	private IDatabase dest;

	FileImport(IDatabase source, IDatabase dest) {
		this.source = source;
		this.dest = dest;
	}

	public void run() {
		log.trace("import external files");
		try {
			File srcDir = source.getFileStorageLocation();
			if (srcDir == null || !srcDir.exists() || !srcDir.isDirectory()
					|| srcDir.listFiles() == null)
				return;
			File destDir = dest.getFileStorageLocation();
			if (destDir == null)
				return;
			if (!destDir.exists())
				destDir.mkdirs();
			syncDirs(srcDir, destDir);
		} catch (Exception e) {
			log.error("failed to import external files", e);
		}
	}

	private void syncDirs(File srcDir, File destDir) throws Exception {
		for (File srcFile : srcDir.listFiles()) {
			File destFile = new File(destDir, srcFile.getName());
			if (srcFile.isDirectory()) {
				if (!destFile.exists()) {
					destFile.mkdirs();
				}
				syncDirs(srcFile, destFile);
			} else if (!destFile.exists()) {
				Files.copy(srcFile, destFile);
			}
		}
	}
}
