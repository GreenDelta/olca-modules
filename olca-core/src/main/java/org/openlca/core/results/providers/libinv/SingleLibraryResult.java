package org.openlca.core.results.providers.libinv;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.results.providers.InversionSolver;
import org.openlca.core.results.providers.LibCache;
import org.openlca.core.results.providers.LibImpactMatrix;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.SolverContext;

import java.util.Map;

class SingleLibraryResult {

	private final SolverContext context;
	private final LibCache libs;

	private SingleLibraryResult(SolverContext context) {
		this.context = context;
		this.libs = LibCache.of(context);
	}

	static ResultProvider calculate(SolverContext context) {
		return new SingleLibraryResult(context).calc();
	}

	private ResultProvider calc() {

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

		var solver =  InversionSolver.of(data)
			.withInverse(libs.matrixOf(lib, LibMatrix.INV))

	}

}
