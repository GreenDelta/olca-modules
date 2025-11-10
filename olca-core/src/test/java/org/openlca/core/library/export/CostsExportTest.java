package org.openlca.core.library.export;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.providers.InMemLibrarySolver;
import org.openlca.core.results.providers.LazyLibrarySolver;
import org.openlca.core.results.providers.SolverContext;
import org.openlca.core.results.providers.libblocks.LibraryInversionSolver;
import org.openlca.npy.Npy;
import org.openlca.util.Dirs;

/// Exports a library and tests calculations using the following example:
/// ```julia
///
///# Q     P    W
/// A = [ 1.0  -0.1  0.0 ;
/// 	   -2.0   1.0  0.0 ;
/// 	    1.0   0.0 -1.0 ]
///
/// f = [ 1.0 ; 0.0 ; 0.0]
///
/// s = A \ f   #=>  [1.25, 2.5, 1.25]
///
/// INV = A^-1
///
///# K are the direct costs in the processes (negative costs are revenues)
/// K = [ -10.0  1.0  0.0 ;
/// 	      5.0 -2.0  0.0 ;
/// 	      3.0  0.0 -1.0 ]
///
///# the net-cost vector that is stored in a library
/// costs = ones((1, 3)) * K  #=> [-2.0 -1.0 -1.0]
///
/// total_costs = costs * s  #=> -6.25
///
///# cost intensities
/// KI = costs * INV   #=> [-6.25 -1.625 1.0]
///```
public class CostsExportTest {

	private final IDatabase db = Tests.getDb();
	private File libRoot;
	private File libDir;

	@Before
	public void setup() throws Exception {
		db.clear();

		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var q = Flow.product("q", mass);
		var p = Flow.product("p", mass);
		var w = Flow.waste("w", mass);
		var eur = Currency.of("EUR");
		eur.referenceCurrency = eur;

		var Q = Process.of("Q", q);
		costs(Q.quantitativeReference, 10.0, eur);
		costs(Q.input(p, 2.0), 5.0, eur);
		costs(Q.output(w, 1.0), 3.0, eur);

		var P = Process.of("P", p);
		costs(P.quantitativeReference, 2.0, eur);
		costs(P.input(q, 0.1), 1.0, eur);

		var W = Process.of("W", w);
		costs(W.quantitativeReference, 1.0, eur);

		db.insert(units, mass, q, p, w, eur, Q, P, W);
		libRoot = Files.createTempDirectory("olca_test").toFile();
		libDir = new File(libRoot, "costlib");
		new LibraryExport(db, libDir)
				.withInversion(true)
				.withCosts(true)
				.run();
		db.clear();
	}

	private void costs(Exchange e, double costs, Currency eur) {
		e.costs = costs;
		e.currency = eur;
	}

	@After
	public void cleanup() {
		db.clear();
		Dirs.delete(libRoot);
	}

	@Test
	public void testCostExport() {
		var costs = Npy.read(new File(libDir, "costs.npy"))
				.asDoubleArray()
				.data();
		assertArrayEquals(new double[]{-2.0, -1.0, -1.0}, costs, 1e-16);

		var costs_i = Npy.read(new File(libDir, "costs_i.npy"))
				.asDoubleArray()
				.data();
		assertArrayEquals(new double[]{-6.25, -1.625, 1.0}, costs_i, 1e-16);
	}

	/// In this test, we create a small product system for each of the
	/// library processes and calculate it. The expected total cost
	/// result for each of these processes is then equal to the cost
	/// intensities of the library (but with a negative value for the
	/// waste flow: [-6.25, -1.625, -1.0]
	@Test
	public void testMountAndCalc() {
		var lib = Library.of(libDir);
		Mounter.of(db, lib).run();

		var expected = new double[]{
				-6.25, -1.625, -1.0
		};
		var names = List.of("Q", "P", "W");

		for (int i = 0; i < expected.length; i++) {
			var name = names.get(i);
			var isWaste = "W".equals(name);

			var proc = db.getForName(Process.class, name);
			var prod = proc.quantitativeReference.flow;
			var refProd = Flow.product("ref_" + name,
					prod.referenceFlowProperty);
			var refProc = Process.of("Ref_" + name, refProd);
			var exchange = isWaste
					? refProc.output(prod, 1)
					: refProc.input(prod, 1);
			exchange.defaultProviderId = proc.id;
			db.insert(refProd, refProc);

			var sys = ProductSystem.of("Sys_" + name, refProc);
			if (isWaste) {
				sys.link(refProc, proc);
			} else {
				sys.link(proc, refProc);
			}
			db.insert(sys);

			// we test this with all possible library result providers
			var setup = CalculationSetup.of(sys)
					.withCosts(true);
			var techIdx = TechIndex.of(db, setup);
			var data = MatrixData.of(db, techIdx)
					.withSetup(setup)
					.build();
			var libs = LibReaderRegistry.of(db,LibraryDir.of(libRoot) );
			var context = SolverContext.of(db, data)
					.withLibraries(libs)
					.withSolver(Tests.getDefaultSolver());

			var providers = List.of(
					LazyLibrarySolver.solve(context),
					InMemLibrarySolver.solve(context),
					LibraryInversionSolver.solve(context)
			);
			for (var p : providers) {
				var r = new LcaResult(p);
				assertEquals("failed with " + p.getClass().getSimpleName(),
						expected[i], r.getTotalCosts(), 1e-16);
			}
		}
	}
}
