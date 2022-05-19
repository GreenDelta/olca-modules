package org.openlca.git.iterator;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jgit.lib.FileMode;
import org.openlca.jsonld.PackageInfo;

import com.google.gson.Gson;

public class SchemaIterator extends EntryIterator {

	public SchemaIterator() {
		super(Arrays.asList(
				new TreeEntry(PackageInfo.FILE_NAME, FileMode.REGULAR_FILE, toByteArray(PackageInfo.create()))));
	}

	private static byte[] toByteArray(PackageInfo info) {
		return new Gson().toJson(info.json()).getBytes(StandardCharsets.UTF_8);
	}

}
