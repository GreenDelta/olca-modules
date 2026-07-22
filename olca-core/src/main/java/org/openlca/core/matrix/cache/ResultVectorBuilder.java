package org.openlca.core.matrix.cache;

import java.util.Optional;

import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.results.providers.ResultProvider;

import gnu.trove.list.array.TDoubleArrayList;
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
/// |-----------------|----------|-----------|-----------|------------|
/// | 1               | 1        | _         | 1         | 0          |
/// | 0               | 1        | 1         | 0         | 1          |
/// | 0               | 1        | 0         | 1         | 0          |
/// | 0               | 0        | 1         | 0         | 1          |
///
public class ResultVectorBuilder {

	private final ImpactMethod method;
	private final boolean regionalized;
	private final MatrixBuildContext ctx;
	private final TLongObjectHashMap<ResultData> data;
	private final NativeSql sql;

	private ResultVectorBuilder(
		CalculationSetup setup, MatrixBuildContext ctx, int size
	) {
		this.method = setup.impactMethod();
		this.regionalized = setup.hasRegionalization();
		this.ctx = ctx;
		this.data = new TLongObjectHashMap<>(size);
		sql = NativeSql.on(ctx.db());
	}

	public static Optional<ResultVectorBuilder> of(
		CalculationSetup setup, TechIndex index, MatrixBuildContext ctx
	) {
		if (index == null || ctx == null || setup == null)
			return Optional.empty();

		TLongObjectHashMap<TechFlow> providers = null;
		for (var p : index) {
			if (!p.isResult())
				continue;
			if (providers == null) {
				providers = new TLongObjectHashMap<>();
			}
			providers.put(p.providerId(), p);
		}

		if (providers == null)
			return Optional.empty();

		var builder = new ResultVectorBuilder(setup, ctx, providers.size());
		builder.fill(providers);
		return Optional.of(builder);
	}

	private void fill(TLongObjectHashMap<TechFlow> providers) {

		// initialize the result data
		var qry = "select id, f_impact_method from tbl_results";
		sql.query(qry, r -> {
			long id = r.getLong(1);
			var provider = providers.get(id);
			if (provider == null)
				return true;
			var datum = new ResultData(
				provider,
				method != null && method.id == r.getLong(2)
			);
			data.put(id, datum);
			return true;
		});

		// fill the result values
		fillImpactValues();
		fillFlowValues();
	}

	private void fillImpactValues() {
		if (method == null)
			return;
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

	private void fillFlowValues() {
		var conversion = ctx.conversions();
		var flows = ctx.flowTable();

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
			from tbl_flow_results
			""";

		sql.query(qry, r -> {
			var datum = data.get(r.getLong(2));
			if (datum == null)
				return true;

			long flowId = r.getLong(3);
			boolean isRef = datum.techFlow.flowId() == flowId;

			if (!isRef && datum.declaresMethod && datum.impacts != null)
				return true;
			var flow = flows.get(flowId);
			if (!isRef && flow.flowType != FlowType.ELEMENTARY_FLOW)
				return true;

			double factor = conversion.forExchange(r.getLong(7), r.getLong(8));
			boolean isInput = r.getBoolean(5);
			double amount = isInput
				? -factor * r.getLong(6)
				: factor * r.getLong(6);

			if (isRef) {
				datum.refAmount = amount;
				return true;
			}

			if (datum.flowIdx == null) {
				datum.flowIdx = regionalized
					? EnviIndex.createRegionalized()
					: EnviIndex.create();
				datum.flowData = new DoubleBuffer();
			}

			long locId;
			var loc = regionalized && (locId = r.getLong(4)) > 0
				? ctx.locations().get(locId)
				: null;

			var enviFlow = isInput
				? EnviFlow.inputOf(flow, loc)
				: EnviFlow.outputOf(flow, loc);
			int idx = datum.flowIdx.add(enviFlow);
			datum.flowData.add(idx, amount);
			return true;
		});
	}

	private static class ResultData {

		final TechFlow techFlow;
		final boolean declaresMethod;

		double refAmount;
		ImpactIndex impactIdx;
		double[] impacts;
		EnviIndex flowIdx;
		DoubleBuffer flowData;

		ResultData(TechFlow techFlow, boolean declaresMethod) {
			this.techFlow = techFlow;
			this.declaresMethod = declaresMethod;
		}

	}

	private record DoubleBuffer(TDoubleArrayList list) {

		DoubleBuffer() {
			this(new TDoubleArrayList());
		}

		void add(int pos, double value) {
			while (list.size() <= pos) {
				list.add(0);
			}
			double current = list.getQuick(pos);
			list.setQuick(pos, current + value);
		}

		double[] toArray() {
			return list.toArray();
		}
	}

	private record ResultVector(
		Demand demand,
		TechIndex techIndex,
		EnviIndex enviIndex,
		ImpactIndex impactIndex,
		double[] flowResults,
		double[] impactResults
	) implements ResultProvider {

		@Override
		public boolean hasCosts() {
			// TODO: not yet supported in result models
			return false;
		}

		@Override
		public double[] scalingVector() {
			return new double[]{1};
		}

		@Override
		public double[] techColumnOf(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			return new double[]{demand.value()};
		}

		@Override
		public double[] solutionOfOne(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			var d = demand.value();
			return d == 0
				? new double[]{0}
				: new double[]{1 / d};
		}

		@Override
		public double loopFactorOf(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			return 1;
		}

		@Override
		public double[] unscaledFlowsOf(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			return flowResults;
		}

		@Override
		public double[] directFlowsOf(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			return flowResults;
		}

		@Override
		public double[] totalFlowsOfOne(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			var demand = demand().value();
			return demand == 0
				? new double[enviIndex.size()]
				: scale(flowResults, 1 / demand);
		}

		@Override
		public double[] totalFlowsOf(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			return flowResults;
		}

		@Override
		public double[] totalFlows() {
			return flowResults;
		}

		@Override
		public double[] impactFactorsOf(int enviFlow) {
			// TODO: not yet implemented
			return new double[impactIndex.size()];
		}

		@Override
		public double[] directImpactsOf(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			return impactResults;
		}

		@Override
		public double[] totalImpactsOfOne(int techFlow) {
			if (techFlow != 0)
				throw new IndexOutOfBoundsException(techFlow);
			var d = demand.value();
			return d == 0
				? new double[impactIndex.size()]
				: scale(impactResults, 1 / d);
		}

		@Override
		public double[] totalImpacts() {
			return impactResults;
		}

		@Override
		public double directCostsOf(int techFlow) {
			// TODO: not yet implemented
			return 0;
		}

		@Override
		public double totalCostsOfOne(int techFlow) {
			// TODO: not yet implemented
			return 0;
		}

		@Override
		public double totalCosts() {
			// TODO: not yet implemented
			return 0;
		}
	}
}
