package org.openlca.core.libraries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.library.LibraryDir;
import org.openlca.util.Dirs;

public class LibraryTest {

	@Test
	public void testInit() throws Exception {
		var dir = Files.createTempDirectory("_olca_lib_test").toFile();
		var libDir = LibraryDir.of(dir);
		assertFalse(libDir.hasLibrary("lib 0.1"));
		libDir.create("lib 0.1");
		assertTrue(libDir.hasLibrary("lib 0.1"));
		var lib = libDir.getLibrary("lib 0.1").orElseThrow();
		var info = lib.getInfo();
		assertEquals("lib 0.1", info.name());
		Dirs.delete(dir);
	}

	@Test
	public void testDependencies() throws Exception {
		var dir = Files.createTempDirectory("_olca_lib_test").toFile();
		var libDir = LibraryDir.of(dir);
		var lib = libDir.create("lib 0.1");
		assertTrue(lib.getDirectDependencies().isEmpty());
		var dep = libDir.create("dep 0.1");
		lib.addDependency(dep);
		assertTrue(lib.getDirectDependencies().contains(dep));
		Dirs.delete(dir);
	}
}
