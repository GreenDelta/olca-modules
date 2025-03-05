package org.openlca.core.results.providers.libblocks;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.results.providers.InversionResult;
import org.openlca.core.results.providers.InversionResultProvider;
import org.openlca.core.results.providers.LibImpactMatrix;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.SolverContext;

class SingleLibraryResult {

	private final SolverContext context;

	private SingleLibraryResult(SolverContext context) {
		this.context = context;
	}

	static ResultProvider of(SolverContext context) {
		return new SingleLibraryResult(context).calculate();
	}

	private ResultProvider calculate() {
		var f = context.data();
		var demand = context.demand();
		var p = demand.techFlow().dataPackage();
		var reader = context.libraries().get(p);

		var data = new MatrixData();
		data.demand = demand;
		data.techIndex = reader.techIndex();
		data.techMatrix = reader.matrixOf(LibMatrix.A);
		data.enviIndex = reader.enviIndex();
		data.enviMatrix = reader.matrixOf(LibMatrix.B);
		data.costVector = reader.costs();

		// note that the LCIA data can be in another library or in the database
		if (MatrixIndex.isPresent(data.enviIndex)
				&& MatrixIndex.isPresent(f.impactIndex)) {
			data.impactMatrix = LibImpactMatrix.of(f.impactIndex, data.enviIndex)
					.build(context.db(), context.libraries());
			data.impactIndex = f.impactIndex;
		}

		var result = InversionResult.of(context.solver(), data)
				.withInverse(reader.matrixOf(LibMatrix.INV))
				.withFlowIntensities(reader.matrixOf(LibMatrix.M))
				.calculate();
		return InversionResultProvider.of(result);
	}

}
