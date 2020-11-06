package org.openlca.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

public class DirsTest {

	@Test
	public void testIsEmpty() throws Exception {
		// the folder must exist
		Assert.assertFalse(Dirs.isEmpty((File) null));
		Assert.assertFalse(Dirs.isEmpty((Path) null));
		Assert.assertFalse(Dirs.isEmpty(new File("does not exist")));
		Assert.assertFalse(Dirs.isEmpty(Paths.get("does not exist")));

		// the folder must be empty
		// Assert.assertTrue(new File("target").exists());
		Assert.assertFalse(Dirs.isEmpty(new File("target")));
		Assert.assertFalse(Dirs.isEmpty(Paths.get("target")));

		var path = Files.createTempDirectory("_olca_tests");
		Assert.assertTrue(Files.isDirectory(path));
		Assert.assertTrue(Dirs.isEmpty(path));
		Assert.assertTrue(Dirs.isEmpty(path.toFile()));
		Dirs.delete(path);
	}

	@Test
	public void testDelete() throws Exception {
		var dir = Files.createTempDirectory("_olca_tests").toFile();
		Assert.assertTrue(Dirs.isEmpty(dir));
		var file = new File(dir, "test.txt");
		Files.writeString(file.toPath(), "a test");
		Dirs.delete(dir);
		Assert.assertFalse(file.exists());
		Assert.assertFalse(dir.exists());
	}
}
