package org.openlca.git.iterator;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jgit.lib.FileMode;
import org.openlca.jsonld.SchemaVersion;

import com.google.gson.Gson;

public class SchemaIterator extends EntryIterator {

	public SchemaIterator() {
		super(Arrays.asList(
				new TreeEntry(SchemaVersion.FILE_NAME, FileMode.REGULAR_FILE, toByteArray(SchemaVersion.current()))));
	}

	private static byte[] toByteArray(SchemaVersion schema) {
		var json = schema.toJson();
		return new Gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

}
