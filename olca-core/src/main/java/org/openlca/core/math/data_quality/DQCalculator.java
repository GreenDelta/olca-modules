package org.openlca.core.math.data_quality;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.math.data_quality.Aggregation.AggregationValue;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.results.ContributionResult;

class DQCalculator {

	private final Map<Long, List<AggregationValue>> flowAggregations = new HashMap<>();
	private final Map<Long, List<AggregationValue>> impactAggregations = new HashMap<>();
	private final Map<LongPair, List<AggregationValue>> impactAggregationsPerFlow = new HashMap<>();
	private final Map<LongPair, List<AggregationValue>> impactAggregationsPerProcess = new HashMap<>();
	private final ContributionResult result;
	private final DQData data;
	private final DQCalculationSetup setup;

	DQCalculator(ContributionResult result, DQData data,
			DQCalculationSetup setup) {
		this.result = result;
		this.data = data;
		this.setup = setup;
	}

	void calculate() {
		for (CategorizedDescriptor process : result.getProcesses()) {
			if (result.flowIndex == null)
				continue;
			result.flowIndex.each((i, f) -> {
				if (f.flow == null)
					return;
				double[] dqValues = data.exchangeData.get(
						LongPair.of(process.id, f.flow.id));
				double flowVal = Math.abs(
						result.getDirectFlowResult(process, f));
				BigDecimal flowResult = new BigDecimal(flowVal);
				if (dqValues == null || flowResult.equals(BigDecimal.ZERO))
					return;
				addValue(flowAggregations, f.flow.id, flowResult, dqValues);
				if (!result.hasImpactResults())
					return;
				addImpactValues(process.id, f.flow.id, flowResult, dqValues);
			});
		}
	}

	private void addImpactValues(long processId, long flowId,
			BigDecimal flowResult, double[] dqValues) {
		for (long impactId : result.impactIndex.ids()) {
			double impactFactor = getImpactFactor(result, impactId, flowId);
			if (impactFactor == 0d)
				continue;
			BigDecimal factor = flowResult
					.multiply(new BigDecimal(impactFactor));
			addValue(impactAggregations, impactId, factor, dqValues);
			addValue(impactAggregationsPerFlow, new LongPair(flowId, impactId),
					factor, dqValues);
			addValue(impactAggregationsPerProcess,
					new LongPair(processId, impactId), factor, dqValues);
		}
	}

	private <T> void addValue(Map<T, List<AggregationValue>> map, T key,
			BigDecimal factor, double[] dqValues) {
		List<AggregationValue> list = safeGetList(key, map);
		int max = setup.exchangeDqSystem.getScoreCount();
		for (int i = 0; i < dqValues.length; i++) {
			double v = dqValues[i];
			if (v != 0d || setup.processingType == ProcessingType.EXCLUDE)
				continue;
			dqValues[i] = max;
		}
		list.add(new AggregationValue(dqValues, factor));
	}

	Map<Long, double[]> getFlowValues() {
		return aggregate(flowAggregations);
	}

	Map<Long, double[]> getImpactValues() {
		return aggregate(impactAggregations);
	}

	Map<LongPair, double[]> getImpactPerProcessValues() {
		return aggregate(impactAggregationsPerProcess);
	}

	Map<LongPair, double[]> getImpactPerFlowValues() {
		return aggregate(impactAggregationsPerFlow);
	}

	private <T> Map<T, double[]> aggregate(Map<T, List<AggregationValue>> map) {
		Map<T, double[]> values = new HashMap<>();
		for (T key : map.keySet()) {
			List<AggregationValue> toAggregate = map.get(key);
			double[] result = Aggregation.applyTo(toAggregate,
					setup.aggregationType);
			values.put(key, result);
		}
		return values;
	}

	private <T> List<AggregationValue> safeGetList(T key,
			Map<T, List<AggregationValue>> map) {
		List<AggregationValue> list = map.get(key);
		if (list != null)
			return list;
		map.put(key, list = new ArrayList<>());
		return list;
	}

	private double getImpactFactor(ContributionResult result, long impactId,
			long flowId) {
		int flowIndex = result.flowIndex.of(flowId);
		int impactIndex = result.impactIndex.of(impactId);
		return result.impactFactors.get(impactIndex, flowIndex);
	}

}
