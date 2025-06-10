package org.openlca.core.library.export;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Libraries;
import org.openlca.core.library.Library;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Dirs;

/// Make sure that in a mixed export of processes and impact categories, all
/// characterization factors are exported.
public class ImpactFactorExportTest {

	private final IDatabase db = Tests.getDb();
	private File libRoot;
	private File libDir;


	@Before
	public void setup() throws Exception {
		db.clear();

		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var ei = Flow.elementary("ei", mass);
		var ej = Flow.elementary("ej", mass);

		var P = Process.of("P", p);
		P.output(ei, 1.0);
		var I = ImpactCategory.of("I");
		I.factor(ei, 2.0);
		I.factor(ej, 3.0);
		var M = ImpactMethod.of("M");
		M.impactCategories.add(I);

		db.insert(units, mass, p, ei, ej, P, I, M);

		libRoot = Files.createTempDirectory("olca_test").toFile();
		libDir = new File(libRoot, "cf-lib");
		new LibraryExport(db, libDir)
				.withInversion(true)
				.run();
		db.clear();
	}

	@After
	public void cleanup() throws Exception {
		db.clear();
		Dirs.delete(libRoot);
	}

	@Test
	public void testImpactFactors() {
		var lib = Library.of(libDir);
		Mounter.of(db, lib).run();
		var I = db.getForName(ImpactCategory.class, "I");
		Libraries.fillFactorsOf(db, LibReader.of(lib, db).create(), I);

		var fi = I.impactFactors.stream()
				.filter(f -> f.flow.name.equals("ei"))
				.findAny()
				.orElseThrow();
		assertEquals(2.0, fi.value, 1e-16);

		var fj = I.impactFactors.stream()
				.filter(f -> f.flow.name.equals("ej"))
				.findAny()
				.orElseThrow();
		assertEquals(3.0, fj.value, 1e-16);
	}

	@Test
	public void testMatrixShapes() {
		var lib = Library.of(libDir);
		Mounter.of(db, lib).run();

		var r = LibReader.of(lib, db).create();
		var A = r.matrixOf(LibMatrix.A);
		assertEquals(1, A.rows());
		assertEquals(1, A.columns());

		var INV = r.matrixOf(LibMatrix.INV);
		assertEquals(1, INV.rows());
		assertEquals(1, INV.columns());

		var B = r.matrixOf(LibMatrix.B);
		assertEquals(2, B.rows());
		assertEquals(1, B.columns());

		var M = r.matrixOf(LibMatrix.M);
		assertEquals(2, M.rows());
		assertEquals(1, M.columns());

		var C = r.matrixOf(LibMatrix.C);
		assertEquals(1, C.rows());
		assertEquals(2, C.columns());
	}
}
