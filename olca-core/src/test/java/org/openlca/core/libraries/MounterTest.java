package org.openlca.core.libraries;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.MountAction;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.PreMountCheck;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Dirs;

public class MounterTest {

	private final IDatabase db = Tests.getDb();
	private  LibraryDir libDir;
	private Library unitLib;
	private UnitGroup units;
	private Library propsLib;
	private FlowProperty prop;

	@Before
	public void setup() throws Exception {
		var dir = Files.createTempDirectory("olca_tests").toFile();
		libDir = new LibraryDir(dir);

		unitLib = libDir.create("units 1.0");
		units = UnitGroup.of("Units of mass", "kg");
		try (var zip = unitLib.openJsonZip()) {
			var exp = new JsonExport(zip);
			exp.write(units);
		}

		propsLib = libDir.create("props 1.0");
		propsLib.addDependency(unitLib);
		prop = FlowProperty.of("Mass", units);
		try (var zip = propsLib.openJsonZip()) {
			var exp = new JsonExport(zip).withReferences(false);
			exp.write(prop);
		}
	}

	@After
	public void tearDown() {
		db.clear();
		Dirs.delete(libDir.folder());
	}

	@Test
	public void testMountWithCheck() {
		var checkResult = PreMountCheck.check(db, propsLib);
		assertFalse(checkResult.isError());
		Mounter.of(db, propsLib)
			.applyDefaultsOf(checkResult)
			.run();
	}

	@Test
	public void testRetag() {
		Mounter.of(db, propsLib).run();
		checkState();
		Mounter.of(db, propsLib)
			.apply(Map.of(
				propsLib, MountAction.RETAG,
				unitLib, MountAction.RETAG))
			.run();
		checkState();
	}

	@Test
	public void testMountWithActions() {
		Mounter.of(db, propsLib)
			.apply(Map.of(
				propsLib, MountAction.ADD,
				unitLib, MountAction.ADD))
			.run();
		checkState();
	}

	@Test
	public void testMountDefault() {
		Mounter.of(db, propsLib).run();
		checkState();
	}

	private void checkState() {
		assertTrue(db.getLibraries().contains("props 1.0"));
		var dbUnits = db.get(UnitGroup.class, units.refId);
		assertEquals(unitLib.name(), dbUnits.library);
		var dbMass = db.get(FlowProperty.class, prop.refId);
		assertEquals(propsLib.name(), dbMass.library);
		var dbLibs = db.getLibraries();
		assertTrue(dbLibs.contains(unitLib.name()));
		assertTrue(dbLibs.contains(propsLib.name()));
	}
}
