package org.openlca.core.libraries;

import static org.junit.Assert.*;

import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.MountCheck;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Dirs;

public class MountCheckTest {

	private final IDatabase db = Tests.getDb();
	private Library lib;

	@Before
	public void setup() throws Exception{
		var dir = Files.createTempDirectory("_olca").toFile();
		lib = Library.of(dir);
	}

	@After
	public void cleanup() {
		Dirs.delete(lib.folder());
	}

	@Test
	public void testOk() throws Exception {
		var group = UnitGroup.of("Units of mass", "kg");
		try (var zip = lib.openJsonZip()) {
			new JsonExport(zip).write(group);
		}
		var state = MountCheck.check(db, lib);
		assertTrue(state.isOk());
		assertFalse(state.isUsed());
		assertFalse(state.isError());
		assertNull(state.error());
	}

	@Test
	public void testUsed() throws Exception {
		var group = UnitGroup.of("Units of mass", "kg");
		db.insert(group);
		try (var zip = lib.openJsonZip()) {
			new JsonExport(zip).write(group);
		}
		var state = MountCheck.check(db, lib);
		assertFalse(state.isOk());
		assertTrue(state.isUsed());
		assertFalse(state.isError());
		assertNull(state.error());
		db.delete(group);
	}
}
