package org.openlca.core.library.export;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.npy.Npy;
import org.openlca.util.Dirs;

/// Exports a library and tests calculations using the following example:
/// ```julia
///
///     # Q     P    W
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
/// # K are the direct costs in the processes (negative costs are revenues)
/// K = [ -10.0  1.0  0.0 ;
/// 	      5.0 -2.0  0.0 ;
/// 	      3.0  0.0 -1.0 ]
///
/// # the net-cost vector that is stored in a library
/// costs = ones((1, 3)) * K  #=> [-2.0 -1.0 -1.0]
///
/// total_costs = costs * s  #=> -6.25
///
/// # cost intensities
/// KI = costs * INV   #=> [-6.25 -1.625 1.0]
/// ```
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
		costs(Q.output(w, 1.0),3.0, eur);

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
		assertArrayEquals(new double[] {-2.0, -1.0, -1.0}, costs, 1e-16);

		var costs_i = Npy.read(new File(libDir, "costs_i.npy"))
				.asDoubleArray()
				.data();
		assertArrayEquals(new double[] {-6.25, -1.625, 1.0}, costs_i, 1e-16);
	}
}
