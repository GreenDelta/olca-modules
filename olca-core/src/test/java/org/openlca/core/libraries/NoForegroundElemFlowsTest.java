package org.openlca.core.libraries;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.LibTechIndex;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryInfo;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;

public class NoForegroundElemFlowsTest {

	@Test
	public void test() throws IOException {
		var db = Tests.getDb();

		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var e1 = Flow.elementary("e1", mass);
		var e2 = Flow.elementary("e2", mass);
		db.insert(units, mass, e1, e2);

		// create stubs for the library processes
		var libProviders = new ArrayList<TechFlow>();
		for (int i = 1; i < 4; i++) {
			var product = db.insert(Flow.product("p" + i, mass));
			var process = Process.of("p" + i, product);
			process.library = "testlib";
			db.insert(process);
			libProviders.add(TechFlow.of(process));
		}
		var techIdx = new TechIndex(libProviders.get(0));
		techIdx.add(libProviders.get(1));
		techIdx.add(libProviders.get(2));

		// create library resources
		var tmpDir = Files.createTempDirectory("_olca_tests");
		var libDir = LibraryDir.of(tmpDir.toFile());
		var lib = Library.create(libDir, LibraryInfo.of("testlib", Version.of(1)));
		LibTechIndex.write(lib, db, techIdx);


		// write the library matrices
		var matrixA = DenseMatrix.of(new double[][]{
			{1.0, -0.1, 0.0},
			{-0.5, 1.0, -0.2},
			{0.0, -0.7, 1.0}
		});
		var matrixB = DenseMatrix.of(new double[][]{
			{ 3.0,  4.0 , 7.0},
			{9.0,  2.0 , 3.0}
		});
		var solver = new JavaSolver();
		var inv =  solver.invert(matrixA);
		var matrixM = solver.multiply(matrixB, inv);
		LibMatrix.A.write(lib, matrixA);
		LibMatrix.B.write(lib, matrixB);
		LibMatrix.INV.write(lib, inv);
		LibMatrix.M.write(lib, matrixM);

		System.out.println(lib.folder);
		db.clear();
	}

}
