package org.openlca.core.results.providers;

public final class ResultProviders {

	private ResultProviders() {
	}

	public static ResultProvider eagerOf(SolverContext context) {
		if (context.hasLibraryLinks())
			return LazyLibraryProvider.of(context);

		var data = context.data();
		var isSmall = data.techMatrix != null
			&& data.techMatrix.rows() < 3000;
		if (isSmall)
			return EagerResultProvider.create(context);
		var solver = context.solver();
		return data.isSparse() && solver.hasSparseSupport()
			? LazyResultProvider.create(context)
			: EagerResultProvider.create(context);
	}

	public static ResultProvider lazyOf(SolverContext context) {
		if (context.hasLibraryLinks())
			return LazyLibraryProvider.of(context);

		var solver = context.solver();
		var data = context.data();
		return data.isSparse() && solver.hasSparseSupport()
			? LazyResultProvider.create(context)
			: EagerResultProvider.create(context);
	}
}
