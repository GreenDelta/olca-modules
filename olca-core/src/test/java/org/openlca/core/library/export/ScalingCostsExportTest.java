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
import org.openlca.core.matrix.index.TechFlow;
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

/// Another test for the export of libraries with cost data. In this example,
/// the processes have a different reference amount than 1.0 and, thus, are
/// scaled to 1.0 in the library export. The costs then need to be also scaled.
///
/// ```julia
///#P   #Q
/// 	A = [ 0.9  -0.7 ; #p
/// 		   -0.2   0.8 ]#q
///
/// 	B = [ 0.7  0.1 ]#r
///
/// 	f = [ 0.0 ; 1.0 ]
///
/// 	s = A f
///# = [1.21, 1.55]
///
/// 	K = [ -1.8   1.4  ;   # costs for p
/// 		     8.0 -32.0  ;   # costs for q in PLN
/// 		     1.4   0.2  ]# costs for r
///
///# net costs in EUR
/// 	costs = [ 1.0  0.25  1.0] * K
///# = [1.6  -6.4]
///
/// 	total_costs = costs * s
///# = -8
///
/// 	direct_costs = costs * diagm(s)
///# = [1.93  -9.93]
///
///   INV = A^-1
/// 	t = diag(A) .* s
/// 	tau = t ./ (diag(A) .* diag(INV))
/// 	# = [0.875, 1.0]
///
/// 	cost_intensities = costs * INV
/// 	# = [0.0  8.0]
///
/// 	upstream_costs = cost_intensities * diagm(tau)
///  	# = = [0.0  8.0]
///```
public class ScalingCostsExportTest {

	private final IDatabase db = Tests.getDb();
	private File libRoot;
	private File libDir;

	@Before
	public void setup() throws Exception {
		db.clear();

		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var r = Flow.elementary("r", mass);
		var eur = Currency.of("EUR");
		var pln = Currency.of("PLN");
		eur.referenceCurrency = eur;
		pln.referenceCurrency = eur;
		pln.conversionFactor = 0.25;

		var P = Process.of("P", p);
		P.quantitativeReference.amount = 0.9;
		costs(P.quantitativeReference, 1.8, eur);
		costs(P.input(q, 0.2), 8.0, pln);
		costs(P.output(r, 0.7), 1.4, eur);

		var Q = Process.of("Q", q);
		Q.quantitativeReference.amount = 0.8;
		costs(Q.quantitativeReference, 32, pln);
		costs(Q.input(p, 0.7), 1.4, eur);
		costs(Q.output(r, 0.1), 0.2, eur);

		db.insert(units, mass, p, q, r, eur, pln, P, Q);
		libRoot = Files.createTempDirectory("olca_test").toFile();
		libDir = new File(libRoot, "colibri");
		new LibraryExport(db, libDir)
				.withInversion(true)
				.withCosts(true)
				.run();
		db.clear();
	}

	private void costs(Exchange e, double costs, Currency currency) {
		e.costs = costs;
		e.currency = currency;
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
		assertArrayEquals(new double[]{1.6 / 0.9, -6.4 / 0.8}, costs, 1e-8);
	}

	@Test
	public void testMountAndCalc() {
		var lib = Library.of(libDir);
		Mounter.of(db, lib).run();

		var Q = db.getForName(Process.class, "Q");
		var q = Q.quantitativeReference.flow;
		var t = Flow.product("t", q.referenceFlowProperty);
		var T = Process.of("T", t);
		T.input(q, 1.0);
		db.insert(t, T);

		var sys = ProductSystem.of(T);
		sys.link(Q, T);
		db.insert(sys);

		var setup = CalculationSetup.of(sys).withCosts(true);
		var techIdx = TechIndex.of(db, setup);
		var data = MatrixData.of(db, techIdx)
				.withSetup(setup)
				.build();
		var libs = LibReaderRegistry.of(db, LibraryDir.of(libRoot));
		var context = SolverContext.of(db, data)
				.withLibraries(libs)
				.withSolver(Tests.getDefaultSolver());

		var providers = List.of(
				LazyLibrarySolver.solve(context),
				InMemLibrarySolver.solve(context),
				LibraryInversionSolver.solve(context)
		);


		var tfT = TechFlow.of(T);
		var tfQ = TechFlow.of(Q);
		var tfP = TechFlow.of(db.getForName(Process.class, "P"));
		var techFlows = List.of(tfT, tfQ, tfP);

		for (var p : providers) {
			var name = p.getClass().getSimpleName();
			var r = new LcaResult(p);
			assertEquals("incorrect total costs with " + name,
					-8.0, r.getTotalCosts(), 1e-8);

			var expectedDirect = new double[]{
					0.0, -9.93103448275862, 1.93103448275862};
			for (int i = 0; i < techFlows.size(); i++) {
				var tf = techFlows.get(i);
				assertEquals("incorrect direct costs with " + name,
						expectedDirect[i], r.getDirectCostsOf(tf), 1e-8);
			}
			var expectedUpstream = new double[]{
					-8.0, -8.0, 0.0};
			for (int i = 0; i < techFlows.size(); i++) {
				var tf = techFlows.get(i);
				assertEquals("incorrect upstream costs with " + name,
						expectedUpstream[i], r.getTotalCostsOf(tf), 1e-8);
			}
		}
	}
}
