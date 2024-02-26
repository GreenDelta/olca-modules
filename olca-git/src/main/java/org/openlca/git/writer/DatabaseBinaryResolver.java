package org.openlca.git.writer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.git.model.Change;
import org.openlca.git.util.BinaryResolver;
import org.openlca.util.Strings;

public class DatabaseBinaryResolver implements BinaryResolver {

	private final FileStore fileStore;

	public DatabaseBinaryResolver(IDatabase database) {
		this.fileStore = new FileStore(database);
	}

	@Override
	public List<String> list(Change change, String relativePath) {
		var root = getFile(change, null).toPath();
		var files = getFile(change, relativePath).listFiles();
		if (files == null)
			return new ArrayList<>();
		return Arrays.asList(files).stream()
				.map(File::toPath)
				.map(root::relativize)
				.map(Path::toString)
				.toList();
	}

	@Override
	public boolean isDirectory(Change change, String relativePath) {
		return getFile(change, relativePath).isDirectory();
	}

	@Override
	public byte[] resolve(Change change, String relativePath) throws IOException {
		return Files.readAllBytes(getFile(change, relativePath).toPath());
	}

	private File getFile(Change change, String relativePath) {
		var folder = fileStore.getFolder(change.type, change.refId);
		if (!Strings.nullOrEmpty(relativePath)) {
			folder = new File(folder, relativePath);
		}
		return folder;
	}

}