package org.openlca.core.results.providers;

import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public final class ResultProviders {

	private ResultProviders() {
	}

	public static ResultProvider eagerOf(SolverContext context) {
		var solver = getSolver();
		if (context.hasLibraryLinks())
			return LazyLibraryProvider.of(context);

		var data = context.matrixData();
		var isSmall = data.techMatrix != null
			&& data.techMatrix.rows() < 3000;
		if (isSmall)
			return EagerResultProvider.create(data);
		return data.isSparse() && solver.hasSparseSupport()
			? LazyResultProvider.create(data)
			: EagerResultProvider.create(data);
	}

	public static ResultProvider lazyOf(SolverContext context) {
		var solver = getSolver();
		if (context.hasLibraryLinks())
			return LazyLibraryProvider.of(context);

		var data = context.matrixData();
		return data.isSparse() && solver.hasSparseSupport()
			? LazyResultProvider.create(data)
			: EagerResultProvider.create(data);
	}

	public static MatrixSolver getSolver() {
		return Julia.isLoaded()
			? new JuliaSolver()
			: new JavaSolver();
	}
}
