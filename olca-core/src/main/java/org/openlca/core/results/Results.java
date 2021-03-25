package org.openlca.core.results;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.results.providers.EagerResultProvider;
import org.openlca.core.results.providers.LazyResultProvider;
import org.openlca.core.results.providers.LibraryResultProvider;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

final class Results {

	private Results() {
	}

	static ResultProvider eagerOf(IDatabase db, MatrixData data) {
		var solver = Julia.isLoaded()
			? new JuliaSolver()
			: new JavaSolver();
		if (data.hasLibraryLinks())
			return LibraryResultProvider.of(db, data);

		var isSmall = data.techMatrix != null
									&& data.techMatrix.rows() < 3000;
		if (isSmall)
			return EagerResultProvider.create(data);
		return data.isSparse() && solver.hasSparseSupport()
			? LazyResultProvider.create(data)
			: EagerResultProvider.create(data);
	}

	static ResultProvider lazyOf(IDatabase db, MatrixData data) {
		var solver = Julia.isLoaded()
			? new JuliaSolver()
			: new JavaSolver();
		if (data.hasLibraryLinks())
			return LibraryResultProvider.of(db, data);
		return data.isSparse() && solver.hasSparseSupport()
			? LazyResultProvider.create(data)
			: EagerResultProvider.create(data);
	}
}
