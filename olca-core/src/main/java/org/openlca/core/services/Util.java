package org.openlca.core.services;

import java.util.Objects;
import java.util.function.BiFunction;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.LcaResult;
import org.openlca.util.Strings;

final class Util {

	static Response<LcaResult> resultOf(CalculationQueue queue, String resultId) {
		if (queue == null)
			return Response.error("no calculation service available");
		try {
			var state = queue.get(resultId);
			if (state.isError())
				return Response.error(state.error());
			if (state.isEmpty())
				return Response.empty();
			return state.isScheduled() || !state.isReady()
					? Response.error("result not yet ready")
					: Response.of(state.result());
		} catch (Exception e) {
			return Response.error(e);
		}
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
		if (Strings.nullOrEmpty(impactId))
			return Response.error("no LCIA category ID provided");
		for (var impact : result.impactIndex()) {
			if (Objects.equals(impactId, impact.refId))
				return Response.of(impact);
		}
		return Response.error("invalid LCIA category ID: " + impactId);
	}

	static <T, U, R> Response<R> join(Response<T> rt, Response<U> ru,
			BiFunction<T, U, Response<R>> fn) {
		if (rt == null || rt.isEmpty() || ru == null || ru.isEmpty())
			return Response.empty();
		if (rt.isError())
			return Response.error(rt.error());
		if (ru.isError())
			return Response.error(ru.error());
		return fn.apply(rt.value(), ru.value());
	}

}
