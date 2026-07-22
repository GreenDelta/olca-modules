package org.openlca.core.matrix.cache;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ImpactMethod;

import gnu.trove.map.hash.TLongObjectHashMap;

/// When results are linked as providers in product systems, this class
/// efficiently loads their values for integrating them in the calculation
/// matrices.
///
/// A result can have impact assessment results (LCIA) and flow results. If it
/// declares the same method as used for the calculation and provides LCIA
/// results, we do not need to load the flow results for the calculation. In
/// other cases, we may use the LCIA results or flow results, depending on which
/// are present:
///
/// | Declares method | Has LCIA | Has flows | Take LCIA | Take flows |
/// |-----------------|----------|-----------|----------|------------|
/// | 1                 | 1         | _          | 1         | 0           |
/// | 0                 | 1         | 1          | 0         | 1           |
/// | 0                 | 1         | 0          | 1         | 0           |
/// | 0                 | 0         | 1          | 0         | 1           |
public class ResultCache {

	private final Map<TechFlow, ResultData> data;

	private ResultCache(Map<TechFlow, ResultData> data) {
		this.data = data;
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public static ResultCache of(
		TechIndex index, CacheContext ctx, ImpactMethod method
	) {
		if (index == null || ctx == null || ctx.db() == null)
			return new ResultCache(Map.of());

		Map<Long, TechFlow> providers = null;
		for (var p : index) {
			if (!p.isResult())
				continue;
			if (providers == null) {
				providers = new HashMap<>();
			}
			providers.put(p.providerId(), p);
		}

		if (providers == null)
			return new ResultCache(Map.of());

		var cache = new ResultCache(new HashMap<>(providers.size()));
		cache.fill(providers, ctx, method);
		return cache;
	}

	private void fill(
		Map<Long, TechFlow> providers, CacheContext ctx, ImpactMethod method
	) {

		// initialize the result data
		var sql = NativeSql.on(ctx.db());
		var data = new TLongObjectHashMap<ResultData>(providers.size());
		var qry = """
			select
			  id, f_reference_flow, f_impact_method
			from tbl_results
			""";
		sql.query(qry, r -> {
			long id = r.getLong(1);
			var provider = providers.get(id);
			if (provider == null)
				return true;
			var datum = new ResultData(
				provider,
				r.getLong(2),
				method != null && method.id == r.getLong(3)
			);
			data.put(id, datum);
			return true;
		});

		// fill the result values
		if (method != null) {
			fillImpactValues(sql, data, method);
		}
		fillFlowValues(ctx, sql, data);
	}

	private void fillImpactValues(
		NativeSql sql, TLongObjectHashMap<ResultData> data, ImpactMethod method
	) {
		var impactIdx = ImpactIndex.of(method);
		if (impactIdx.isEmpty())
			return;

		var qry = """
			select
			  f_result, f_impact_category, amount
			from tbl_impact_results
			""";
		sql.query(qry, r -> {
			var datum = data.get(r.getLong(1));
			if (datum == null)
				return true;
			var idx = impactIdx.of(r.getLong(2));
			if (idx < 0)
				return true;

			if (datum.impacts == null) {
				datum.impactIdx = impactIdx;
				datum.impacts = new double[impactIdx.size()];
			}
			datum.impacts[idx] = r.getDouble(3);
			return true;
		});
	}

	private void fillFlowValues(
		CacheContext ctx, NativeSql sql, TLongObjectHashMap<ResultData> data
	) {
		var conversion = ctx.conversions();

		var qry = """
			select
			  id,
			  f_result,
			  f_flow,
			  f_location,
			  is_input,
			  resulting_amount_value,
			  f_unit,
			  f_flow_property_factor
			""";

		sql.query(qry, r -> {
			var datum = data.get(r.getLong(2));
			if (datum == null)
				return true;
			boolean isRef = r.getLong(1) == datum.qRef;
			if (!isRef && datum.declaresMethod && datum.impacts != null)
				return true;

			double factor = conversion.forExchange(r.getLong(7), r.getLong(8));


			return true;
		});

	}


	private static class ResultData {

		final TechFlow techFlow;
		final long qRef;
		final boolean declaresMethod;

		ImpactIndex impactIdx;
		double[] impacts;

		ResultData(TechFlow techFlow, long qRef, boolean declaresMethod) {
			this.techFlow = techFlow;
			this.qRef = qRef;
			this.declaresMethod = declaresMethod;
		}

	}

	private record FlowEntry(
		long flowId,
		long locationId,
		boolean isInput,
		double amount
	) {
	}

	private record ImpactEntry(
		long impactId,
		double amount
	) {
	}

}
