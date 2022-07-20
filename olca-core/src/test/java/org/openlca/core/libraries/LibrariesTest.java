package org.openlca.core.libraries;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.ToIntFunction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.io.index.IxFormat;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.core.matrix.io.index.IxTechItem;
import org.openlca.core.library.Libraries;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.Mounter;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Dirs;

public class LibrariesTest {

	private final IDatabase db = Tests.getDb();
	private LibraryDir libDir;

	@Before
	public void setup() throws IOException  {
		var dir = Files.createTempDirectory("_olca_tests").toFile();
		libDir = LibraryDir.of(dir);
	}

	@After
	public void cleanup() {
		Dirs.delete(libDir.folder());
	}

	@Test
	public void testEmptyIndices() {
		var lib = libDir.create("a test lib");
		assertTrue(lib.readTechIndex().isEmpty());
		assertTrue(lib.readEnviIndex().isEmpty());
		assertTrue(lib.readImpactIndex().isEmpty());
		assertTrue(lib.syncTechIndex(db).isEmpty());
		assertTrue(lib.syncEnviIndex(db).isEmpty());
		assertTrue(lib.syncImpactIndex(db).isEmpty());
	}

	@Test
	public void testSyncTechIndex() throws IOException {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var P = Process.of("P", p);
		var q = Flow.product("q", mass);
		var Q = Process.of("Q", q);

		var lib = libDir.create("lib");
		try(var zip = lib.openJsonZip()) {
			var exp = new JsonExport(zip);
			List.of(units, mass, p, q, P, Q)
				.forEach(exp::write);
		}

		var libIdx = IxTechIndex.of(
			IxTechItem.of(0, P, p),
			IxTechItem.of(1, Q, q));
		libIdx.writeToDir(lib.folder(), IxFormat.CSV);

		Mounter.of(db, lib).run();
		var techIdx = lib.syncTechIndex(db).orElse(null);
		assertNotNull(techIdx);
	}

	@Test
	public void testDependencyOrder() {

		var a = libDir.create("A");
		var b = libDir.create("B");
		b.addDependency(a);
		var c = libDir.create("C");
		c.addDependency(b);
		var d = libDir.create("D");
		d.addDependency(a);
		var e = libDir.create("E");
		e.addDependency(c);
		e.addDependency(d);

		var sorted = Libraries.dependencyOrderOf(e);
		ToIntFunction<String> pos = s -> {
			for (int i = 0; i < sorted.size(); i++) {
				if (sorted.get(i).name().equals(s))
					return i;
			}
			return -1;
		};

		assertEquals(5, sorted.size());
		assertEquals(0, pos.applyAsInt("A"));
		assertEquals(4, pos.applyAsInt("E"));
		assertTrue(pos.applyAsInt("B") < pos.applyAsInt("C"));
	}

}
