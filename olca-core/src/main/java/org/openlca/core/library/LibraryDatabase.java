package org.openlca.core.library;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.openlca.core.database.Derby;
import org.openlca.util.Dirs;

/**
 * A utility class for creating a database with a mounted library (and possible
 * library dependencies). The database is cached as `_db_.zolca` file in the
 * library folder so that it can be quickly reused to create fresh databases.
 */
public class LibraryDatabase {

	private final Library lib;

	private LibraryDatabase(Library lib) {
		this.lib = Objects.requireNonNull(lib);
	}

	public static LibraryDatabase of(Library lib) {
		return new LibraryDatabase(lib);
	}

	/**
	 * Creates the `_db_.zolca` cache file in the library folder if it does not
	 * exist yet.
	 *
	 * @return the cache file of the database with the mounted library
	 */
	public File getOrCreateCacheFile() {
		var file = new File(lib.folder(), "_db_.zolca");
		if (file.exists() && file.isFile())
			return file;
		try {
			var tempDir = Files.createTempDirectory("_lib_db_").toFile();
			try (var db = new Derby(tempDir)) {
				Mounter.of(db, lib).run();
			}
			zip(tempDir, file);
			return file;
		} catch (Exception e) {
			throw new RuntimeException("failed to create cache file", e);
		}
	}

	public void createFresh(File folder) {
		Objects.requireNonNull(folder);
		Dirs.delete(folder);
		var cacheFile = getOrCreateCacheFile();
		try (var zip = new ZipFile(cacheFile)) {
			for (var es = zip.entries(); es.hasMoreElements();) {
				var entry = es.nextElement();
				if (entry.isDirectory())
					continue;
				var file = new File(folder, entry.getName()).toPath();
				Files.createDirectories(file.getParent());
				try (var stream = zip.getInputStream(entry)) {
					Files.copy(stream, file);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to extract cache file", e);
		}
	}

	private void zip(File root, File target) throws Exception {

		record Item(String path, File dir) {
		}
		var queue = new ArrayDeque<Item>();
		queue.add(new Item("", root));

		try (var out = new FileOutputStream(target);
				 var zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {

			while (!queue.isEmpty()) {
				var next = queue.poll();
				var files = next.dir.listFiles();
				if (files == null)
					continue;
				for (var file : files) {
					if (file.isDirectory()) {
						var path = next.path + file.getName() + "/";
						queue.add(new Item(path, file));
						continue;
					}
					zip.putNextEntry(new ZipEntry(next.path + file.getName()));
					Files.copy(file.toPath(), zip);
					zip.closeEntry();
				}
			}
		}
	}
}
