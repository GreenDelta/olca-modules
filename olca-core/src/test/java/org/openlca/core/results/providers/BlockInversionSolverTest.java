package org.openlca.core.results.providers;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibEnviIndex;
import org.openlca.core.library.LibEnviItem;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.LibTechIndex;
import org.openlca.core.library.LibTechItem;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.results.providers.libblocks.BlockInversionSolver;
import org.openlca.util.Dirs;

import java.nio.file.Files;
import java.util.UUID;


public class BlockInversionSolverTest {

	private final IDatabase db = Tests.getDb();
	private LibraryDir libDir;
	private Library lib;

	@Before
	public void setup() throws Exception {
		var dir = Files.createTempDirectory("olca_tests").toFile();
		libDir = LibraryDir.of(dir);
		lib = libDir.create(UUID.randomUUID().toString());
	}

	@After
	public void cleanup() {
		Dirs.delete(libDir.folder());
	}

	@Test
	public void testSimpleBlocks() {
		var units = withLib(UnitGroup.of("Mass units", "kg"));
		var mass = withLib(FlowProperty.of("Mass", units));
		var e = withLib(Flow.elementary("e", mass));
		var p = withLib(Flow.product("p", mass));
		var pP = withLib(Process.of("P", p));
		var q = db.insert(Flow.product("q", mass));
		var qQ = db.insert(Process.of("Q", q));

		// write library data
		LibMatrix.A.write(lib, DenseMatrix.of(new double[][]{{1}}));
		LibMatrix.INV.write(lib, DenseMatrix.of(new double[][]{{1}}));
		LibMatrix.B.write(lib, DenseMatrix.of(new double[][]{{1}}));
		LibMatrix.M.write(lib, DenseMatrix.of(new double[][]{{1}}));
		LibTechIndex.of(LibTechItem.of(0, pP, p)).writeTo(lib);
		LibEnviIndex.of(LibEnviItem.output(0, e)).writeTo(lib);

		// the foreground data
		var data = new MatrixData();
		data.demand = Demand.of(TechFlow.of(qQ), 1);
		data.techIndex = TechIndex.of(
			TechFlow.of(qQ), TechFlow.of(pP));
		data.enviIndex = EnviIndex.create();
		data.enviIndex.add(EnviFlow.outputOf(Descriptor.of(e)));
		data.techMatrix = DenseMatrix.of(new double[][]{
			{1, 0},
			{-1, 1}
		});
		data.enviMatrix = DenseMatrix.of(new double[][]{
			{1, 0}
		});

		var context = SolverContext.of(db, data)
			.libraryDir(libDir);

		var result = BlockInversionSolver.solve(context);
		assertArrayEquals(new double[]{2}, result.totalFlows(), 1e-16);

		db.delete(qQ, pP, q, p, e, mass, units);
	}

	private <T extends RootEntity> T withLib(T entity) {
		entity.library = lib.name();
		return db.insert(entity);
	}

}
