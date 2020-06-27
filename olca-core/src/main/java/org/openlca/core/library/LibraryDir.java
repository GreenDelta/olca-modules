package org.openlca.core.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.jsonld.Json;

/**
 * A library directory is a specific folder where each sub-folder is a library.
 * If a library A in that folder has a dependency to a library B there should
 * be a sub-folder with that library B in the same directory. The identifier
 * of a library, which is the combination of name and version, is used as the
 * folder name of a library.
 */
public class LibraryDir {

	public final File dir;

	public LibraryDir(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("the folder " + dir
						+ " does not exist and could not be created");
			}
		}
		this.dir = dir;
	}

	public List<Library> getLibraries() {
		var files = dir.listFiles();
		if (files == null)
			return Collections.emptyList();
		var libs = new ArrayList<Library>();
		for (var file : files) {
			if (!file.isDirectory())
				continue;
			var meta = new File(file, "library.json");
			if (!meta.exists())
				continue;
			var json = Json.readObject(meta);
			if (json.isEmpty())
				continue;
			libs.add(Library.fromJson(json.get()));
		}
		return libs;
	}

	/**
	 * Get the folder of the given library in this library directory. This
	 * folder may not exist yet.
	 */
	public File getFolder(Library library) {
		return library == null || library.name == null
				? new File(dir, "_null")
				: new File(dir, library.id());
	}
}
