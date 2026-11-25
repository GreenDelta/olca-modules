package org.openlca.core.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.export.LibraryExport;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Dirs;

/// Mounts a library on a database that already contains the library's data
/// sets as background system (the library was exported from that database),
/// with a product system that links into this background system.
public class LibraryLinkCleanupTest {

	private final IDatabase db = Tests.getDb();
	private LibraryDir libDir;
	private Library lib;

	private Flow e;
	private Process F, A, B, C, D;
	private ProductSystem system;

	@Before
	public void setup() throws Exception {
		db.clear();
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		e = Flow.elementary("e", mass);
		var p1 = Flow.product("p1", mass);
		var p2 = Flow.product("p2", mass);
		var p3 = Flow.product("p3", mass);
		db.insert(units, mass, e, p1, p2, p3);

		// background: A <- B <- C, with a process parameter in B
		C = Process.of("C", p3);
		C.output(e, 2.0);
		B = Process.of("B", p2);
		B.input(p3, 1.0);
		B.output(e, 1.0);
		B.parameters.add(Parameter.process("x", 1.0));
		A = Process.of("A", p1);
		A.input(p2, 1.0);
		A.output(e, 1.0);
		db.insert(C, B, A);

		// export the background database as library
		libDir = new LibraryDir(Files.createTempDirectory("olca_tests").toFile());
		new LibraryExport(db, new File(libDir.folder(), "bg")).run();
		lib = libDir.getLibrary("bg").orElseThrow();

		// foreground: F <- A, and an unlinked process D
		var f = db.insert(Flow.product("f", mass));
		F = Process.of("F", f);
		F.input(p1, 1.0);
		F.output(e, 1.0);
		D = Process.of("D", db.insert(Flow.product("d", mass)));
		db.insert(F, D);

		system = ProductSystem.of(F)
			.link(A, F)
			.link(B, A)
			.link(C, B);
		system.processes.add(D.id);
		system.parameterSets.add(ParameterRedefSet.of(
			"baseline", ParameterRedef.of(B.parameters.getFirst(), B, 2.0)));
		system = db.insert(system);
	}

	@After
	public void tearDown() {
		db.clear();
		Dirs.delete(libDir.folder());
	}

	@Test
	public void testMountCleansSystem() {
		assertEquals(5.0, totalE(false), 1e-10);
		assertEquals(3, system.processLinks.size());
		long version = system.version;

		var check = PreMountCheck.check(db, lib);
		assertEquals(
			PreMountState.TAG_CONFLICT, check.getState(lib).orElseThrow());
		Mounter.of(db, lib).applyDefaultsOf(check).run();

		var s = db.get(ProductSystem.class, system.id);

		// only the foreground link F <- A is kept
		assertEquals(1, s.processLinks.size());
		var link = s.processLinks.getFirst();
		assertEquals(F.id, link.processId);
		assertEquals(A.id, link.providerId);

		// B and C are removed, the unlinked process D is kept
		assertEquals(Set.of(F.id, A.id, D.id), s.processes);

		// the redefinition of the library parameter is deleted
		assertTrue(s.parameterSets.getFirst().parameters.isEmpty());

		// the change is visible
		assertTrue(s.version > version);

		// the result is the same, now calculated with the library
		assertEquals(5.0, totalE(true), 1e-10);
	}

	@Test
	public void testRetagCleansSystem() {
		Mounter.of(db, lib)
			.apply(lib, MountAction.RETAG)
			.run();
		var s = db.get(ProductSystem.class, system.id);
		assertEquals(1, s.processLinks.size());
		assertEquals(Set.of(F.id, A.id, D.id), s.processes);
		assertTrue(s.parameterSets.getFirst().parameters.isEmpty());
		assertEquals(5.0, totalE(true), 1e-10);
	}

	@Test
	public void testIdempotent() {
		Mounter.of(db, lib).run();
		long version = db.get(ProductSystem.class, system.id).version;

		var cleanup = LibraryLinkCleanup.of(db);
		cleanup.run();
		assertTrue(cleanup.changedSystems().isEmpty());
		assertEquals(version, db.get(ProductSystem.class, system.id).version);
	}

	@Test
	public void testNoCleanupWithoutLibraryData() {
		var cleanup = LibraryLinkCleanup.of(db);
		cleanup.run();
		assertTrue(cleanup.changedSystems().isEmpty());
		var s = db.get(ProductSystem.class, system.id);
		assertEquals(3, s.processLinks.size());
		assertEquals(system.version, s.version);
		assertFalse(s.parameterSets.getFirst().parameters.isEmpty());
	}

	private double totalE(boolean withLibrary) {
		var s = db.get(ProductSystem.class, system.id);
		var calculator = new SystemCalculator(db);
		if (withLibrary) {
			calculator.withLibraries(libDir);
		}
		var result = calculator.calculate(CalculationSetup.of(s));
		var idx = result.enviIndex().stream()
			.filter(i -> i.flow().id == e.id)
			.findAny()
			.orElseThrow();
		return result.getTotalFlowValueOf(idx);
	}
}
