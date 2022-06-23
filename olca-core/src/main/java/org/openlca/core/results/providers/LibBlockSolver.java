package org.openlca.core.results.providers;

import java.util.Map;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.MatrixIndex;

public class LibBlockSolver {

	private final SolverContext context;
	private final LibCache libs;

	public static ResultProvider solve(SolverContext context) {
		return new LibBlockSolver(context).solve();
	}

	private LibBlockSolver(SolverContext context) {
		this.context = context;
		this.libs = LibCache.of(context);
	}

	private ResultProvider solve() {
		var f = context.data();
		var demand = f.demand;
		if (demand.techFlow().isFromLibrary()) {
			return buildSingleLibraryResult();
		}

		return null;
	}

	private ResultProvider buildSingleLibraryResult() {

		var f = context.data();
		var demand = context.demand();
		var lib = demand.techFlow().library();

		var data = new MatrixData();
		data.demand = demand;
		data.techIndex = libs.techIndexOf(lib);
		data.techMatrix = libs.matrixOf(lib, LibMatrix.A);
		data.enviIndex = libs.enviIndexOf(lib);
		data.enviMatrix = libs.matrixOf(lib, LibMatrix.B);
		data.costVector = libs.costsOf(lib).orElse(null);

		// not that the LCIA data can be in another library or in the database
		if (MatrixIndex.isPresent(data.enviIndex)
			&& MatrixIndex.isPresent(f.impactIndex)) {
			data.impactMatrix = LibImpactMatrix.of(f.impactIndex, data.enviIndex)
				.withLibraryEnviIndices(Map.of(lib, data.enviIndex))
				.build(context.db(), context.libraryDir());
			data.impactIndex = f.impactIndex;
		}

		var result =  InversionResult.of(context.solver(), data)
			.withInverse(libs.matrixOf(lib, LibMatrix.INV))
			.withInventoryIntensities(libs.matrixOf(lib, LibMatrix.M))
			.calculate();
		return InversionResultProvider.of(result);
	}
}
