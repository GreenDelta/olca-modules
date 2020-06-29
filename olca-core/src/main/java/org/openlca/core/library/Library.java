package org.openlca.core.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.util.Pair;

public class Library {

	/**
	 * The folder where the library files are stored.
	 */
	public final File folder;

	public Library(File folder) {
		this.folder = folder;
	}

	public LibraryInfo getInfo() {
		var file = new File(folder, "library.json");
		var obj = Json.readObject(file);
		if (obj.isEmpty())
			throw new RuntimeException("failed to read " + file);
		return LibraryInfo.fromJson(obj.get());
	}


}
