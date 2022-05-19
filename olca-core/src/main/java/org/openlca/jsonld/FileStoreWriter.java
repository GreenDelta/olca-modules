package org.openlca.jsonld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import org.openlca.util.Strings;

public record FileStoreWriter(File root) implements JsonStoreWriter {

	public FileStoreWriter(File root) {
		this.root = Objects.requireNonNull(root);
		if (!root.exists()) {
			try {
				Files.createDirectories(root.toPath());
			} catch (IOException e) {
				throw new RuntimeException("failed to create folder: " + root, e);
			}
		}
		var infoFile = new File(root, PackageInfo.FILE_NAME);
		if (!infoFile.exists()) {
			PackageInfo.create().writeTo(this);
		}
	}

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

}
