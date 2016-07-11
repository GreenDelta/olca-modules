package org.openlca.core.math.data_quality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.data_quality.Aggregation.AggregationValue;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.DQSystem;
import org.openlca.core.results.ContributionResult;

public class DQResult {

	public final DQSystem processSystem;
	public final DQSystem exchangeSystem;
	public final AggregationType aggregationType;
	private Map<Long, int[]> processValues = new HashMap<>();
	private Map<Long, int[]> flowValues = new HashMap<>();
	private Map<Long, int[]> impactValues = new HashMap<>();
	private Map<LongPair, int[]> flowValuesPerProcess = new HashMap<>();
	private Map<LongPair, int[]> impactValuesPerProcess = new HashMap<>();

	public int[] getProcessQuality(long processId) {
		return processValues.get(processId);
	}

	public int[] getFlowQuality(long flowId) {
		return flowValues.get(flowId);
	}

	public int[] getImpactQuality(long impactCategoryId) {
		return impactValues.get(impactCategoryId);
	}

	public int[] getFlowQuality(long processId, long flowId) {
		return flowValuesPerProcess.get(new LongPair(processId, flowId));
	}

	public int[] getImpactQuality(long processId, long impactCategoryId) {
		return impactValuesPerProcess.get(new LongPair(processId, impactCategoryId));
	}

	public static DQResult calculate(IDatabase db, ContributionResult result, AggregationType type, long productSystemId) {
		if (db == null || result == null || type == null || productSystemId == 0l)
			return null;
		DQData data = DQData.load(db, productSystemId);
		if (data.processSystem == null && data.exchangeSystem == null)
			return null;
		DQResult dqResult = new DQResult(data.processSystem, data.exchangeSystem, type);
		if (data.processSystem != null) {
			dqResult.processValues = data.processData;
		}
		if (data.exchangeSystem == null)
			return dqResult;
		dqResult.flowValuesPerProcess = data.exchangeData;
		if (type == AggregationType.NONE)
			return dqResult;
		dqResult.calculateFlowValues(result, type, data);
		if (!result.hasImpactResults())
			return dqResult;
		dqResult.calculateImpactValues(result, type, data);
		dqResult.calculateImpactValuesPerProcess(result, type, data);
		return dqResult;
	}

	private void calculateFlowValues(ContributionResult result, AggregationType type, DQData data) {
		for (long flowId : result.flowIndex.getFlowIds()) {
			List<AggregationValue> toAggregate = new ArrayList<>();
			for (long processId : result.productIndex.getProcessIds()) {
				double flowResult = Math.abs(result.getSingleFlowResult(processId, flowId));
				if (flowResult == 0d)
					continue;
				int[] values = data.exchangeData.get(new LongPair(processId, flowId));
				if (values == null)
					continue;
				toAggregate.add(new AggregationValue(values, flowResult));
			}
			flowValues.put(flowId, Aggregation.applyTo(toAggregate, type));
		}
	}

	private void calculateImpactValues(ContributionResult result, AggregationType type, DQData data) {
		for (long impactId : result.impactIndex.getKeys()) {
			List<AggregationValue> toAggregate = new ArrayList<>();
			for (long flowId : result.flowIndex.getFlowIds()) {
				double impactFactor = Math.abs(getImpactFactor(result, impactId, flowId));
				if (impactFactor == 0d)
					continue;
				int[] values = flowValues.get(flowId);
				if (values == null)
					continue;
				toAggregate.add(new AggregationValue(values, impactFactor));
			}
			impactValues.put(impactId, Aggregation.applyTo(toAggregate, type));
		}
	}

	private void calculateImpactValuesPerProcess(ContributionResult result, AggregationType type, DQData data) {
		for (long impactId : result.impactIndex.getKeys()) {
			for (long processId : result.productIndex.getProcessIds()) {
				List<AggregationValue> toAggregate = new ArrayList<>();
				for (long flowId : result.flowIndex.getFlowIds()) {
					double flowResult = Math.abs(result.getSingleFlowResult(processId, flowId));
					if (flowResult == 0d)
						continue;
					double impactFactor = Math.abs(getImpactFactor(result, impactId, flowId));
					if (impactFactor == 0d)
						continue;
					int[] values = data.exchangeData.get(new LongPair(processId, flowId));
					if (values == null)
						continue;
					toAggregate.add(new AggregationValue(values, flowResult));
				}
				impactValuesPerProcess.put(new LongPair(processId, impactId), Aggregation.applyTo(toAggregate, type));
			}
		}
	}

	private double getImpactFactor(ContributionResult result, long impactId, long flowId) {
		int flowIndex = result.flowIndex.getIndex(flowId);
		int impactIndex = result.impactIndex.getIndex(impactId);
		return result.impactFactors.getEntry(impactIndex, flowIndex);

	}

	private DQResult(DQSystem processSystem, DQSystem exchangeSystem, AggregationType type) {
		this.processSystem = processSystem;
		this.exchangeSystem = exchangeSystem;
		this.aggregationType = type;
	}

}
