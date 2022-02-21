package org.openlca.jsonld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;


/**
 * Reads JSON data sets from files in a folder structure.
 */
public record FileStoreReader(File root) implements JsonStoreReader {

	@Override
	public byte[] getBytes(String path) {
		if (Strings.nullOrEmpty(path))
			return null;
		try {
			var file = new File(root, path);
			if (!file.exists())
				return null;
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to read @" + path, e);
		}
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		if (type == null)
			return Collections.emptyList();
		var dir = new File(root, ModelPath.folderOf(type));
		if (!dir.exists())
			return Collections.emptyList();
		var files = dir.list();
		if (files == null)
			return Collections.emptyList();
		var ids = new ArrayList<String>(files.length);
		for (var f : files) {
			if (f.endsWith(".json")) {
				ids.add(f.substring(0, f.length() - 5));
			}
		}
		return ids;
	}

	@Override
	public List<String> getFiles(String dir) {
		var folder = new File(root, dir);
		if (!folder.exists())
			return Collections.emptyList();
		var files = folder.listFiles();
		if (files == null)
			return Collections.emptyList();
		return Arrays.stream(files)
			.filter(File::isFile)
			.map(File::getAbsolutePath)
			.toList();
	}
}
