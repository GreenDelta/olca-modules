package org.openlca.core.libraries;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Dirs;

import java.io.File;
import java.nio.file.Files;

public class DepTest {

	private final IDatabase db = Tests.getDb();
	private  LibraryDir libDir;

	@Before
	public void setup() throws Exception {
		var dir = Files.createTempDirectory("olca_tests").toFile();
		libDir = new LibraryDir(dir);
	}

	@After
	public void tearDown() {
		db.clear();
		Dirs.delete(libDir.folder());
	}

	@Test
	public void testDep() throws Exception {

		var unitLib = libDir.create("units 1.0");
		var units = UnitGroup.of("Units of mass", "kg");
		try (var zip = ZipStore.open(new File(unitLib.folder(), "meta.zip"))) {
			var exp = new JsonExport(zip);
			exp.write(units);
		}

		var propsLib = libDir.create("props 1.0");
		propsLib.addDependency(unitLib);
		var prop = FlowProperty.of("Mass", units);
		try (var zip = ZipStore.open(new File(propsLib.folder(), "meta.zip"))) {
			var exp = new JsonExport(zip);
			exp.write(prop);
		}

		propsLib.mountTo(db);

		assertTrue(db.getLibraries().contains("units 1.0"));
		assertTrue(db.getLibraries().contains("props 1.0"));

	}

}
