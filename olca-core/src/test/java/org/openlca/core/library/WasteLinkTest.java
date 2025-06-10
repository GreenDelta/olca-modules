package org.openlca.core.library;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Dirs;

public class WasteLinkTest {

	private final IDatabase db = Tests.getDb();
	private LibraryDir libRoot;

	@Before
	public void setup() throws IOException {
		var dir = Files.createTempDirectory("_olca_tests").toFile();
		libRoot = LibraryDir.of(dir);
	}

	@After
	public void cleanup() {
		Dirs.delete(libRoot.folder());
	}

	@Test
	public void testWasteLink() {

		db.clear();
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var e = Flow.elementary("e", mass);
		var w = Flow.waste("w", mass);
		var W = Process.of("W", w);
		W.output(e, 1.0);
		db.insert(units, mass, e, w, W);

		var libDir = new File(libRoot.folder(), "lib0");
		new LibraryExport(db, libDir).run();
		var lib = libRoot.getLibrary("lib0").orElseThrow();
		db.clear();
		Mounter.of(db, lib).run();

		W = db.getForName(Process.class, "W");
		mass = db.getForName(FlowProperty.class, "Mass");
		e = db.getForName(Flow.class, "e");
		assertNotNull(W);
		assertNotNull(mass);
		assertNotNull(e);

		var p = Flow.product("p", mass);
		var P = Process.of("P", p);
		P.output(e, 1.0);
		P.output(W.quantitativeReference.flow, 1.0);
		db.insert(p, P);

		var resultP = new SystemCalculator(db)
				.withLibraries(libRoot)
				.calculate(CalculationSetup.of(P));
		var ei = resultP.enviIndex().stream()
				.filter(i -> i.flow().name.equals("e"))
				.findAny()
				.orElseThrow();
		assertEquals(2.0, resultP.getTotalFlowValueOf(ei), 1e-16);

		var resultW = new SystemCalculator(db)
				.withLibraries(libRoot)
				.calculate(CalculationSetup.of(W));
		assertEquals(1.0, resultW.getTotalFlowValueOf(ei), 1e-16);

		db.clear();
	}

}
