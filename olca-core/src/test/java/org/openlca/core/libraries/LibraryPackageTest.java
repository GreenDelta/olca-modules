package org.openlca.core.libraries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryPackage;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.util.Dirs;

public class LibraryPackageTest {

	@Test
	public void testZipUnzip() throws Exception {

		var dir = Files.createTempDirectory("_olca_lib_test").toFile();

		// create a library with one dependency
		var libDir = LibraryDir.of(dir);
		var lib = libDir.initLibrary("lib 0.1");
		var dep = libDir.initLibrary("dep 0.1");
		lib.addDependency(dep);

		// put a tech. matrix into the libraries
		var matrix = new DenseMatrix(10, 10);
		NpyMatrix.write(lib.folder(), "A", matrix);
		NpyMatrix.write(dep.folder(), "A", matrix);

		// package the library and its dependency
		var zipFile = Files.createTempFile("_olca_test", ".zip").toFile();
		LibraryPackage.zip(lib, zipFile);
		Dirs.delete(dir);
		assertFalse(dir.exists());

		// check the zip
		var packInfo = LibraryPackage.getInfo(zipFile);
		assertEquals("lib", packInfo.name());
		assertTrue(packInfo.dependencies().contains(dep.id()));

		// extract the zip
		dir = Files.createTempDirectory("_olca_lib_test").toFile();
		libDir = LibraryDir.of(dir);
		LibraryPackage.unzip(zipFile, libDir);
		assertTrue(libDir.hasLibrary("lib 0.1"));
		assertTrue(libDir.hasLibrary("dep 0.1"));

		Dirs.delete(dir);
		assertTrue(zipFile.delete());
	}
}
