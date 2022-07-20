package org.openlca.core.libraries;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.io.index.IxContext;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.util.Dirs;

public class NoForegroundElemFlowsTest {

	@Test
	public void test() throws IOException {
		// init the library
		var tmpDir = Files.createTempDirectory("_olca_tests");
		var libDir = LibraryDir.of(tmpDir.toFile());
		var lib = libDir.create("testlib 1");
		var db = Tests.getDb();

		// create the reference data
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var e1 = Flow.elementary("e1", mass);
		var e2 = Flow.elementary("e2", mass);
		db.insert(units, mass, e1, e2);
		var enviIndex = EnviIndex.create();
		enviIndex.add(EnviFlow.inputOf(FlowDescriptor.of(e1)));
		enviIndex.add(EnviFlow.outputOf(FlowDescriptor.of(e2)));

		// create stubs for the library processes
		var libProviders = new ArrayList<TechFlow>();
		Process libProcess = null;
		for (int i = 1; i < 4; i++) {
			var product = db.insert(Flow.product("p" + i, mass));
			var process = Process.of("p" + i, product);
			process.library = lib.name();
			db.insert(process);
			libProviders.add(TechFlow.of(process));
			if (i == 2) {
				libProcess = process;
			}
		}
		var techIdx = new TechIndex(libProviders.get(0));
		techIdx.add(libProviders.get(1));
		techIdx.add(libProviders.get(2));

		// create the foreground system
		var product = db.insert(Flow.product("fp", mass));
		var process = Process.of("fp", product);
		process.input(libProcess.quantitativeReference.flow, 0.5);
		db.insert(process);
		var system = ProductSystem.of(process)
			.link(libProcess, process);
		system.targetAmount = 2;
		db.insert(system);

		// create library resources

		var ctx = IxContext.of(db);
		IxTechIndex.of(techIdx, ctx).writeToDir(lib.folder());
		IxEnviIndex.of(enviIndex, ctx).writeToDir(lib.folder());

		// write the library matrices
		var matrixA = DenseMatrix.of(new double[][]{
			{1.0, -0.1, 0.0}, // p1
			{-0.5, 1.0, -0.2}, // p2
			{0.0, -0.7, 1.0} // p3
		});
		var matrixB = DenseMatrix.of(new double[][]{
			{-3.0, -4.0, -7.0}, // e1
			{9.0, 2.0, 3.0} // e2
		});
		var solver = new JavaSolver();
		var inv = solver.invert(matrixA);
		var matrixM = solver.multiply(matrixB, inv);
		LibMatrix.A.write(lib, matrixA);
		LibMatrix.B.write(lib, matrixB);
		LibMatrix.INV.write(lib, inv);
		LibMatrix.M.write(lib, matrixM);

		// calculate the results
		var setup = CalculationSetup.fullAnalysis(system);
		var result = new SystemCalculator(db)
			.withLibraryDir(libDir)
			.calculateFull(setup);

		// check the result
		var expected = matrixM.getColumn(1);
		var totals = result.totalFlowResults();
		for (int i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], totals[i], 1e-16);
		}

		db.clear();
		Dirs.delete(libDir.folder());
	}

}
