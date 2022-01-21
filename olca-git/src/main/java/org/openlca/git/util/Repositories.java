package org.openlca.git.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.internal.storage.file.FileRepository;

public final class Repositories {

	private Repositories() {
	}

	public static FileRepository open(File dir) {
		return open(dir.toPath());
	}

	public static FileRepository open(Path dir) {
		try {
			var repo = new FileRepository(dir.toFile());
			if (!Files.exists(dir)) {
				// create a new bare repository
				repo.create(true);
			}
			return repo;
		} catch (IOException e) {
			throw new RuntimeException(
				"could not get repository from folder: " + dir, e);
		}
	}

}
