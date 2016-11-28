package org.openlca.core.math.data_quality;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.math.data_quality.Aggregation.AggregationValue;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.results.ContributionResult;

class DQCalculator {

	private final Map<Long, List<AggregationValue>> flowAggregations = new HashMap<>();
	private final Map<Long, List<AggregationValue>> impactAggregations = new HashMap<>();
	private final Map<LongPair, List<AggregationValue>> impactAggregationsPerFlow = new HashMap<>();
	private final Map<LongPair, List<AggregationValue>> impactAggregationsPerProcess = new HashMap<>();
	private final ContributionResult result;
	private final DQData data;
	private final DQCalculationSetup setup;

	public DQCalculator(ContributionResult result, DQData data, DQCalculationSetup setup) {
		this.result = result;
		this.data = data;
		this.setup = setup;
	}

	void calculate() {
		for (long processId : result.productIndex.getProcessIds()) {
			for (long flowId : result.flowIndex.getFlowIds()) {
				addValues(processId, flowId);
			}
		}
	}

	private void addValues(long processId, long flowId) {
		double[] dqValues = getDqValues(processId, flowId);
		BigDecimal flowResult = new BigDecimal(getFlowResult(processId, flowId));
		if (dqValues == null || flowResult.equals(BigDecimal.ZERO))
			return;
		addValue(flowAggregations, flowId, flowResult, dqValues);
		if (!result.hasImpactResults())
			return;
		addImpactValues(processId, flowId, flowResult, dqValues);
	}

	private void addImpactValues(long processId, long flowId, BigDecimal flowResult, double[] dqValues) {
		for (long impactId : result.impactIndex.getKeys()) {
			double impactFactor = getImpactFactor(result, impactId, flowId);
			if (impactFactor == 0d)
				continue;
			BigDecimal factor = flowResult.multiply(new BigDecimal(impactFactor));
			addValue(impactAggregations, impactId, factor, dqValues);
			addValue(impactAggregationsPerFlow, new LongPair(flowId, impactId), factor, dqValues);
			addValue(impactAggregationsPerProcess, new LongPair(processId, impactId), factor, dqValues);
		}
	}

	private <T> void addValue(Map<T, List<AggregationValue>> map, T key, BigDecimal factor, double[] dqValues) {
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

	private double[] getDqValues(long processId, long flowId) {
		return data.exchangeData.get(new LongPair(processId, flowId));
	}

	private double getFlowResult(long processId, long flowId) {
		return Math.abs(result.getSingleFlowResult(processId, flowId));
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
			double[] result = Aggregation.applyTo(toAggregate, setup.aggregationType);
			values.put(key, result);
		}
		return values;
	}

	private <T> List<AggregationValue> safeGetList(T key, Map<T, List<AggregationValue>> map) {
		List<AggregationValue> list = map.get(key);
		if (list != null)
			return list;
		map.put(key, list = new ArrayList<>());
		return list;
	}

	private double getImpactFactor(ContributionResult result, long impactId, long flowId) {
		int flowIndex = result.flowIndex.getIndex(flowId);
		int impactIndex = result.impactIndex.getIndex(impactId);
		return result.impactFactors.get(impactIndex, flowIndex);
	}

}
