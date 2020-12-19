package org.openlca.core.libraries;

import static org.junit.Assert.*;

import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryInfo;
import org.openlca.util.Dirs;

public class LibraryTest {

	@Test
	public void testInit() throws Exception {
		var dir = Files.createTempDirectory("_olca_lib_test").toFile();
		var libDir = LibraryDir.of(dir);
		var libInfo = LibraryInfo.of("lib", "0.1");
		assertFalse(libDir.exists(libInfo));
		libDir.init(libInfo);
		assertTrue(libDir.exists(libInfo));
		Dirs.delete(dir);
	}

	@Test
	public void testDependencies() throws Exception {
		var dir = Files.createTempDirectory("_olca_lib_test").toFile();
		var libDir = LibraryDir.of(dir);
		var libInfo = LibraryInfo.of("lib", "0.1");
		var lib = libDir.init(libInfo);
		assertTrue(lib.getDependencies().isEmpty());
		var depInfo = LibraryInfo.of("dep", "0.1");
		var dep = libDir.init(depInfo);
		lib.addDependency(dep);
		assertTrue(lib.getDependencies().contains(dep));
		Dirs.delete(dir);
	}
}
