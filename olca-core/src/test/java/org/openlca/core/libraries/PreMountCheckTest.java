package org.openlca.core.libraries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.PreMountCheck;
import org.openlca.core.library.PreMountState;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Dirs;

public class PreMountCheckTest {

	private final IDatabase db = Tests.getDb();
	private Library lib;

	@Before
	public void setup() throws Exception {
		var dir = Files.createTempDirectory("_olca").toFile();
		lib = Library.of(dir);
	}

	@After
	public void cleanup() {
		Dirs.delete(lib.folder());
	}

	@Test
	public void testNew() {
		with(group -> PreMountState.NEW);
	}

	@Test
	public void testPresent() {
		with(group -> {
			group.library = lib.name();
			db.insert(group);
			return PreMountState.PRESENT;
		});
	}

	@Test
	public void testTagConflict() {
		with(group -> {
			group.library = "another lib";
			db.insert(group);
			return  PreMountState.TAG_CONFLICT;
		});
	}

	@Test
	public void testError() {
		var result = PreMountCheck.check(db, null);
		assertTrue(result.isError());
		assertNotNull(result.error());
	}

	private void with(Function<UnitGroup, PreMountState> fn) {
		try {
			var group = UnitGroup.of("Units of mass", "kg");
			try (var zip = lib.openJsonZip()) {
				new JsonExport(zip).write(group);
			}
			var expected = fn.apply(group);
			var result = PreMountCheck.check(db, lib);
			assertFalse(result.isError());
			assertEquals(expected, result.getState(lib).orElseThrow());
			var synced = db.get(UnitGroup.class, group.id);
			if (synced != null) {
				db.delete(synced);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
