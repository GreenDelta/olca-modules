package org.openlca.core.services;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.agroups.AnalysisGroupResult;

/// Guards the access to a calculated result in the calculation queue. Results
/// are not thread-safe, but different requests can access the same result from
/// different threads, and the result can be disposed while a request is still
/// running. Thus, all access to the result and its disposal run under the same
/// lock of this handle. The handle is shared by all copies of a [ResultState]
/// of that result.
final class ResultHandle {

	private final CalculationSetup setup;
	private final LcaResult result;
	private final ReentrantLock lock = new ReentrantLock();

	private boolean isDisposed;
	private AnalysisGroupResult groups;

	ResultHandle(CalculationSetup setup, LcaResult result) {
		this.setup = setup;
		this.result = result;
	}

	/// Applies the given function on the result while holding the lock of
	/// this handle. Returns an error if the result was already disposed.
	<T> Response<T> apply(Function<LcaResult, Response<T>> fn) {
		lock.lock();
		try {
			return isDisposed
				? Response.error("the result was disposed")
				: fn.apply(result);
		} finally {
			lock.unlock();
		}
	}

	/// Applies the given function on the result and its analysis group
	/// results while holding the lock of this handle. Returns an empty
	/// response if there are no analysis groups.
	<T> Response<T> applyWithGroups(Function<Util.GroupResult, Response<T>> fn) {
		return apply(r -> {
			var g = groups();
			return g.isEmpty()
				? Response.empty()
				: fn.apply(new Util.GroupResult(r, g));
		});
	}

	/// Must be only called under the lock.
	private AnalysisGroupResult groups() {
		if (groups != null)
			return groups;
		groups = setup != null
			&& setup.target() instanceof ProductSystem sys
			&& !sys.analysisGroups.isEmpty()
			? AnalysisGroupResult.of(sys, result)
			: AnalysisGroupResult.empty();
		return groups;
	}

	/// Disposes the result. If a request currently accesses the result, this
	/// method waits until that request is finished. Calling this method more
	/// than once has no effect.
	void dispose() {
		lock.lock();
		try {
			if (isDisposed)
				return;
			isDisposed = true;
			result.dispose();
		} finally {
			lock.unlock();
		}
	}
}
