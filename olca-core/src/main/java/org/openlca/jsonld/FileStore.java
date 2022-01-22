package org.openlca.jsonld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

/**
 * An implementation of the EntityStore interface that maps data sets to files
 * in a folder.
 */
public record FileStore(File root) implements JsonStoreReader, JsonStoreWriter {

	@Override
	public void put(String path, byte[] data) {
		if (Strings.nullOrEmpty(path) || data == null)
			return;
		try {
			var file = new File(root, path);
			var dir = file.getParentFile();
			if (!dir.exists()) {
				Files.createDirectories(dir.toPath());
			}
			Files.write(file.toPath(), data);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to write file " + path, e);
		}
	}

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
	public List<String> getBinFiles(ModelType type, String refId) {
		var modelPath = ModelPath.binFolderOf(type, refId);
		var dir = new File(root, modelPath);
		if (!dir.exists())
			return Collections.emptyList();
		var files = dir.list();
		return files == null
			? Collections.emptyList()
			: Arrays.stream(files)
			.map(file -> modelPath + "/" + file)
			.collect(Collectors.toList());
	}
}
