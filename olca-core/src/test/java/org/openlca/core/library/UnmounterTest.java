package org.openlca.core.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.index.IxContext;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.util.Dirs;

/// Test the unmounter with the following system,
/// where the processes P and W are in a library:
///
/// ```julia
///     #  Q    P    W
/// A = [  1.0  0.0  0.0 ;  # q
///       -1.0  1.0 -0.1 ;  # p
///        0.5  0.5 -1.0 ]  # w
///
/// B = [  1.0  2.0  3.0 ]  # e
///
/// f = [ 1.0 ; 0.0 ; 0.0 ]
///
/// g = B * (A \ f)  # = 6.368421
/// ```
public class UnmounterTest {

	private final IDatabase db = Tests.getDb();
	private ProductSystem system;
	private LibraryDir libDir;
	private Flow e;

	@Before
	public void setup() throws IOException {
		// library data
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		e = Flow.elementary("e", mass);
		var p = Flow.product("p", mass);
		var w = Flow.waste("w", mass);
		var P = Process.of("P", p);
		var W = Process.of("W", w);
		for (var xi : List.of(units, mass, e, p, w, P, W)) {
			xi.library = "lib";
			db.insert(xi);
		}

		// process Q
		var q = Flow.product("q", mass);
		var Q = Process.of("Q", q);
		Q.input(p, 1.0);
		Q.output(w, 0.5);
		Q.output(e, 1.0);
		db.insert(q, Q);

		// the product system
		system = ProductSystem.of("S", Q);
		system.link(P, Q);
		system.link(Q, W);
		db.insert(system);

		// create the library
		var tempDir = Files.createTempDirectory("_olca_tests");
		libDir = LibraryDir.of(tempDir.toFile());
		var lib = libDir.create("lib");
		db.addLibrary("lib");
		var ctx = IxContext.of(db);

		// matrix indices
		var techIdx = new TechIndex();
		techIdx.add(TechFlow.of(P));
		techIdx.add(TechFlow.of(W));
		IxTechIndex.of(techIdx, ctx).writeToDir(lib.folder());
		var enviIndex = EnviIndex.create();
		enviIndex.add(EnviFlow.outputOf(FlowDescriptor.of(e)));
		IxEnviIndex.of(enviIndex, ctx).writeToDir(lib.folder());

		// matrices
		var matrixA = DenseMatrix.of(new double[][] {
				{1.0, -0.1},
				{0.5, -1.0}
		});
		LibMatrix.A.write(lib, matrixA);
		var matrixB = DenseMatrix.of(new double[][] {
				{2.0, 3.0}
		});
		LibMatrix.B.write(lib, matrixB);
	}

	@After
	public void cleanup() {
		Dirs.delete(libDir.folder());
		var refs = List.of(
				e.referenceFlowProperty,
				e.referenceFlowProperty.unitGroup
		);
		var flows = new HashSet<Flow>();
		var processes = new HashSet<Process>();
		for (var pid : system.processes) {
			var p = db.get(Process.class, pid);
			processes.add(p);
			for (var e : p.exchanges) {
				flows.add(e.flow);
			}
		}

		db.delete(system);
		for (var p : processes) {
			db.delete(p);
		}
		for (var f : flows) {
			db.delete(f);
		}
		for (var ref : refs) {
			db.delete(ref);
		}
	}

	@Test
	public void testMountedResult() {
		var setup = CalculationSetup.of(system);
		var result = new SystemCalculator(db)
				.withLibraries(libDir)
				.calculate(setup);
		var r = result.getTotalFlowValueOf(EnviFlow.outputOf(e));
		assertEquals(6.368421, r, 1e-6);
	}

	@Test
	public void testUnmountedResult() {
		var lib = libDir.getLibrary("lib").orElseThrow();
		var libReader = LibReader.of(lib, db).create();
		Unmounter.keepAll(db, libReader);

		// reload e and the system; check that all is unmounted
		e = db.get(Flow.class, e.id);
		assertFalse(e.isFromLibrary());
		system = db.get(ProductSystem.class, system.id);
		Process P = null, W = null;
		for (var pid : system.processes) {
			var p = db.get(Process.class, pid);
			if (p.name.equals("P")) {
				P = p;
			} else if (p.name.equals("W")) {
				W = p;
			}
			assertFalse(p.isFromLibrary());
			for (var e : p.exchanges) {
				assertFalse(e.flow.isFromLibrary());
			}
		}

		// the library internal links are not contained in the
		// system, so we have to manually link the processes
		system.link(P, W);
		system.link(W, P);
		system = db.update(system);

		var setup = CalculationSetup.of(system);
		var result = new SystemCalculator(db)
				.calculate(setup);
		var r = result.getTotalFlowValueOf(EnviFlow.outputOf(e));
		assertEquals(6.368421, r, 1e-6);
	}

}
