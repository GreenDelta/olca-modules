package org.openlca.core.math.data_quality;

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

	public DQCalculator(ContributionResult result, DQData data) {
		this.result = result;
		this.data = data;
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
		double flowResult = getFlowResult(processId, flowId);
		if (dqValues == null || flowResult == 0d)
			return;
		addValue(flowAggregations, flowId, flowResult, dqValues);
		if (!result.hasImpactResults())
			return;
		addImpactValues(processId, flowId, flowResult, dqValues);
	}

	private void addImpactValues(long processId, long flowId, double flowResult, double[] dqValues) {
		for (long impactId : result.impactIndex.getKeys()) {
			double impactFactor = getImpactFactor(result, impactId, flowId);
			if (impactFactor == 0d)
				continue;
			double factor = flowResult * impactFactor;
			addValue(impactAggregations, impactId, factor, dqValues);
			addValue(impactAggregationsPerFlow, new LongPair(flowId, impactId), factor, dqValues);
			addValue(impactAggregationsPerProcess, new LongPair(processId, impactId), factor, dqValues);
		}
	}

	private <T> void addValue(Map<T, List<AggregationValue>> map, T key, double factor, double[] dqValues) {
		List<AggregationValue> list = safeGetList(key, map);
		list.add(new AggregationValue(dqValues, factor));
	}

	private double[] getDqValues(long processId, long flowId) {
		return data.exchangeData.get(new LongPair(processId, flowId));
	}

	private double getFlowResult(long processId, long flowId) {
		return Math.abs(result.getSingleFlowResult(processId, flowId));
	}

	Map<Long, double[]> getFlowValues(AggregationType aggregationType) {
		return aggregate(flowAggregations, aggregationType);
	}

	Map<Long, double[]> getImpactValues(AggregationType aggregationType) {
		return aggregate(impactAggregations, aggregationType);
	}

	Map<LongPair, double[]> getImpactPerProcessValues(AggregationType aggregationType) {
		return aggregate(impactAggregationsPerProcess, aggregationType);
	}
	
	Map<LongPair, double[]> getImpactPerFlowValues(AggregationType aggregationType) {
		return aggregate(impactAggregationsPerFlow, aggregationType);
	}

	private <T> Map<T, double[]> aggregate(Map<T, List<AggregationValue>> map, AggregationType aggregationType) {
		Map<T, double[]> values = new HashMap<>();
		for (T key : map.keySet()) {
			List<AggregationValue> toAggregate = map.get(key);
			double[] result = Aggregation.applyTo(toAggregate, aggregationType);
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
		return result.impactFactors.getEntry(impactIndex, flowIndex);
	}
	
	
}
