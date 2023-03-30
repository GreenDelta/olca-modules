package org.openlca.io.olca;

import java.io.File;
import java.nio.file.Files;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.util.Dirs;

/**
 * Copies the files from the file storage of the source database to the file
 * storage of the destination database.
 */
class FileImport {

	private final ImportLog log;
	private final IDatabase source;
	private final IDatabase dest;

	private FileImport(Config conf) {
		this.log = conf.log();
		this.source = conf.source();
		this.dest = conf.target();
	}

	static void run(Config conf) {
		new FileImport(conf).run();
	}

	private void run() {
		try {
			var srcDir = source.getFileStorageLocation();
			if (srcDir == null
					|| !srcDir.exists()
					|| !srcDir.isDirectory())
				return;
			var destDir = dest.getFileStorageLocation();
			if (destDir == null)
				return;
			Dirs.createIfAbsent(destDir);
			syncDirs(srcDir, destDir);
		} catch (Exception e) {
			log.error("failed to import external files", e);
		}
	}

	private void syncDirs(File srcDir, File destDir) throws Exception {
		var srcFiles = srcDir.listFiles();
		if (srcFiles == null)
			return;
		for (var srcFile : srcFiles) {
			var destFile = new File(destDir, srcFile.getName());
			if (srcFile.isDirectory()) {
				Dirs.createIfAbsent(destFile);
				syncDirs(srcFile, destFile);
			} else if (!destFile.exists()) {
				Files.copy(srcFile.toPath(), destFile.toPath());
			}
		}
	}
}
