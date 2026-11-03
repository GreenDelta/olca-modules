package org.openlca.core.services;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.openlca.commons.Strings;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.agroups.AnalysisGroupResult;
import org.openlca.core.services.ResultState.Empty;
import org.openlca.core.services.ResultState.Failed;
import org.openlca.core.services.ResultState.Ready;
import org.openlca.core.services.ResultState.Scheduled;

final class Util {

	/// Applies the given function on the result with the given ID under the
	/// lock of that result, see [Ready#withResult(Function)].
	static <T> Response<T> withResult(
		CalculationQueue queue, String resultId,
		Function<LcaResult, Response<T>> fn
	) {
		return withReady(queue, resultId, ready -> ready.withResult(fn));
	}

	/// Applies the given function on the result and analysis group results
	/// with the given ID under the lock of that result.
	static <T> Response<T> withGroupResult(
		CalculationQueue queue, String resultId,
		Function<GroupResult, Response<T>> fn
	) {
		return withReady(queue, resultId, ready -> ready.withGroupResult(fn));
	}

	/// Applies the given function on the state with the given ID if that
	/// state is ready. Otherwise, it returns an empty response or an error.
	static <T> Response<T> withReady(
		CalculationQueue queue, String resultId,
		Function<Ready, Response<T>> fn
	) {
		if (queue == null)
			return Response.error("no calculation service available");
		try {
			return switch (queue.get(resultId)) {
				case Ready ready -> fn.apply(ready);
				case Scheduled _ -> Response.error("result not ready yet");
				case Failed failed -> Response.error(failed.error());
				case Empty _ -> Response.empty();
			};
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	record GroupResult(LcaResult result, AnalysisGroupResult groups) {
	}

	static Response<TechFlow> techFlowOf(LcaResult result, TechFlowId techFlowId) {
		if (result == null)
			return Response.error("no result available");
		if (techFlowId == null)
			return Response.error("no tech-flow ID provided");
		var techFlow = techFlowId.findTechFlowOf(result).orElse(null);
		return techFlow == null
			? Response.error("invalid tech-flow ID: " + techFlowId)
			: Response.of(techFlow);
	}

	static Response<EnviFlow> enviFlowOf(LcaResult result, EnviFlowId enviFlowId) {
		if (result == null)
			return Response.error("no result available");
		if (enviFlowId == null)
			return Response.error("no envi-flow ID provided");
		var enviFlow = enviFlowId.findEnviFlowOf(result).orElse(null);
		return enviFlow == null
			? Response.error("invalid envi-flow ID: " + enviFlowId)
			: Response.of(enviFlow);
	}

	static Response<ImpactDescriptor> impactCategoryOf(
		LcaResult result, String impactId) {
		if (result == null)
			return Response.error("no result available");
		if (!result.hasImpacts())
			return Response.error("result has no LCIA results");
		if (Strings.isBlank(impactId))
			return Response.error("no LCIA category ID provided");
		for (var impact : result.impactIndex()) {
			if (Objects.equals(impactId, impact.refId))
				return Response.of(impact);
		}
		return Response.error("invalid LCIA category ID: " + impactId);
	}

	static <T, U, R> Response<R> join(
		Response<T> rt, Response<U> ru, BiFunction<T, U, Response<R>> fn) {
		if (rt == null || rt.isEmpty() || ru == null || ru.isEmpty())
			return Response.empty();
		if (rt.isError())
			return Response.error(rt.error());
		if (ru.isError())
			return Response.error(ru.error());
		return fn.apply(rt.value(), ru.value());
	}
}
