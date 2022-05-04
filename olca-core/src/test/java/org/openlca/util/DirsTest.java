package org.openlca.util;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class DirsTest {

	@Test
	public void testDeleteWithReadOnlyFile() throws Exception {
		var dir = Files.createTempDirectory("_olca_tests").toFile();
		var file = new File(dir, "file");
		assertTrue(file.createNewFile());
		assertTrue(file.setReadOnly());
		assertTrue(file.exists());

		// this would fail with an AccessDeniedException on Windows
		// Files.delete(file.toPath());
		Dirs.delete(dir);
		assertFalse(dir.exists());
		assertFalse(file.exists());
	}

	@Test
	public void testDeleteFile() throws Exception {
		var file = Files.createTempFile("_olca_tests", ".txt");
		assertTrue(Files.exists(file));
		Dirs.delete(file);
		assertFalse(Files.exists(file));
	}

	@Test
	public void testIsEmpty() throws Exception {
		// the folder must exist
		assertFalse(Dirs.isEmpty((File) null));
		assertFalse(Dirs.isEmpty((Path) null));
		assertFalse(Dirs.isEmpty(new File("does not exist")));
		assertFalse(Dirs.isEmpty(Paths.get("does not exist")));

		// the folder must be empty
		// Assert.assertTrue(new File("target").exists());
		assertFalse(Dirs.isEmpty(new File("target")));
		assertFalse(Dirs.isEmpty(Paths.get("target")));

		var path = Files.createTempDirectory("_olca_tests");
		assertTrue(Files.isDirectory(path));
		assertTrue(Dirs.isEmpty(path));
		assertTrue(Dirs.isEmpty(path.toFile()));
		Dirs.delete(path);
	}

	@Test
	public void testDelete() throws Exception {
		var dir = Files.createTempDirectory("_olca_tests").toFile();
		assertTrue(Dirs.isEmpty(dir));
		var file = new File(dir, "test.txt");
		Files.writeString(file.toPath(), "a test");
		Dirs.delete(dir);
		assertFalse(file.exists());
		assertFalse(dir.exists());
	}
}
