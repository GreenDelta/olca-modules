package org.openlca.core.libraries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryPackage;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Dirs;

public class LibraryPackageTest {

	@Test
	public void testZipUnzip() throws Exception {

		var dir = Files.createTempDirectory("_olca_lib_test").toFile();

		// create a library with one dependency
		var libDir = LibraryDir.of(dir);
		var lib = libDir.create("lib 0.1");
		var dep = libDir.create("dep 0.1");
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
		assertEquals("lib 0.1", packInfo.name());
		assertTrue(packInfo.dependencies().contains(dep.name()));

		// extract the zip
		dir = Files.createTempDirectory("_olca_lib_test").toFile();
		libDir = LibraryDir.of(dir);
		LibraryPackage.unzip(zipFile, libDir);
		assertTrue(libDir.hasLibrary("lib 0.1"));
		assertTrue(libDir.hasLibrary("dep 0.1"));

		Dirs.delete(dir);
		assertTrue(zipFile.delete());
	}

	@Test
	public void testDependencyUnpack() throws Exception {
		var baseDir = Files.createTempDirectory("_olca").toFile();
		System.out.println(baseDir.getAbsolutePath());
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);

		// library with units
		var sourceDir = LibraryDir.of(new File(baseDir, "source"));
		var unitLib = sourceDir.create("units");
		try (var zip = unitLib.openJsonZip()) {
			new JsonExport(zip).write(units);
		}

		// library with flow properties
		var propLib = sourceDir.create("props");
		propLib.addDependency(unitLib);
		try (var zip = propLib.openJsonZip()) {
			new JsonExport(zip)
				.withReferences(false)
				.write(mass);
		}

		// pack and unpack
		var pack = new File(baseDir, "pack.zip");
		LibraryPackage.zip(propLib, pack);
		var targetDir = LibraryDir.of(new File(baseDir, "target"));
		LibraryPackage.unzip(pack, targetDir);
		unitLib = targetDir.getLibrary("units").orElseThrow();
		propLib = targetDir.getLibrary("props").orElseThrow();

		// check unpacked libraries
		assertTrue(propLib.getDirectDependencies().contains(unitLib));
		try (var zip = unitLib.openJsonZip()) {
			var json = zip.get(ModelType.UNIT_GROUP, units.refId);
			assertEquals("Units of mass", Json.getString(json, "name"));
		}
		try (var zip = propLib.openJsonZip()) {
			var json = zip.get(ModelType.FLOW_PROPERTY, mass.refId);
			assertEquals("Mass", Json.getString(json, "name"));
		}

		Dirs.delete(baseDir);
	}
}
