package org.openlca.julia;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the Java interface for the native Julia libraries and contains some
 * utility methods for loading these libraries.
 */
public final class Julia {

	private static AtomicBoolean _loaded = new AtomicBoolean(false);

	/** Returns true if the Julia libraries with openLCA bindings are loaded. */
	public static boolean loaded() {
		return _loaded.get();
	}

	/**
	 * Loads the libraries from a folder specified by the "OLCA_JULIA"
	 * environment variable.
	 */
	public static boolean load() {
		if (loaded())
			return true;
		Logger log = LoggerFactory.getLogger(Julia.class);
		String path = System.getenv("OLCA_JULIA");
		if (path == null || path.isEmpty()) {
			log.warn("Could not load Julia libs and bindings;"
					+ "OLCA_JULIA is not defined");
			return false;
		}
		File dir = new File(path);
		return load(dir);
	}

	/**
	 * Loads the Julia libraries and openLCA bindings from the given folder.
	 * Returns true if the libraries could be loaded (at least there should be a
	 * `libjolca` library in the folder that could be loaded).
	 */
	public static boolean load(File dir) {
		Logger log = LoggerFactory.getLogger(Julia.class);
		log.info("Try to load Julia libs and bindings from {}", dir);
		if (loaded()) {
			log.info("Julia libs already loaded; do nothing");
			return true;
		}
		if (!containsBindings(dir)) {
			log.warn("{} does not contain the openLCA bindings libjolca", dir);
			return false;
		}
		try {
			for (File file : dir.listFiles()) {
				System.load(file.getAbsolutePath());
			}
			_loaded.set(true);
			return true;
		} catch (Exception e) {
			log.error("Failed to load Julia libs from " + dir, e);
			return false;
		}
	}

	private static boolean containsBindings(File dir) {
		if (dir == null || !dir.exists())
			return false;
		for (File lib : dir.listFiles()) {
			if (!lib.isFile())
				continue;
			if (lib.getName().contains("libjolca"))
				return true;
		}
		return false;
	}

}
