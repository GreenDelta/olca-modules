package org.openlca.core.database.derby;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;

class Derby {

	private Derby() {
	}

	/**
	 * Returns true if the given folder is (most likely) a Derby database by
	 * checking if Derby specific files and folders are present; see the Derby
	 * folder specification:
	 * http://db.apache.org/derby/docs/10.0/manuals/develop/develop13.html
	 */
	static boolean isDerbyFolder(File folder) {
		if (folder == null || !folder.exists() || !folder.isDirectory())
			return false;
		File log = new File(folder, "log");
		if (!log.exists() || !log.isDirectory())
			return false;
		File seg0 = new File(folder, "seg0");
		if (!seg0.exists() || !seg0.isDirectory())
			return false;
		File props = new File(folder, "service.properties");
		if (!props.exists() || !props.isFile())
			return false;
		return true;
	}

	/**
	 * Searches for a database dump in the given folder. Returns null if nothing
	 * was found. Otherwise the returned path can be directly used to restore an
	 * in-memory database.
	 */
	static String searchDump(String path) {
		if (path == null)
			return null;
		File root = new File(path);
		if (!root.exists() || !root.isDirectory())
			return null;
		Queue<File> dirs = new ArrayDeque<>();
		dirs.add(root);
		while (!dirs.isEmpty()) {
			File dir = dirs.poll();
			if (isDerbyFolder(dir)) {
				String dbPath = dir.getAbsolutePath();
				return dbPath.replace('\\', '/');
			}
			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					dirs.add(f);
				}
			}
		}
		return null;
	}

}
