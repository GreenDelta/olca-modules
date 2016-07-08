package org.openlca.core.math.data_quality;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.DQSystem;
import org.openlca.core.results.ContributionResult;

public class DQResult {

	public final DQSystem processSystem;
	public final DQSystem exchangeSystem;
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

	public static DQResult calculate(IDatabase db, ContributionResult result, long productSystemId) {
		DQData data = DQData.load(db, productSystemId);
		DQResult dqResult = new DQResult(data.processSystem, data.exchangeSystem);
		dqResult.processValues = data.processData;
		dqResult.calculateFlowValues(result, data);
		dqResult.calculateImpactValues(result, data);
		dqResult.flowValuesPerProcess = data.exchangeData;
		dqResult.calculateImpactValuesPerProcess(result, data);
		return dqResult;
	}

	private void calculateFlowValues(ContributionResult result, DQData data) {
		for (long flowId : result.flowIndex.getFlowIds()) {
			double[] aggregated = new double[exchangeSystem.indicators.size()];
			int[] divisor = new int[exchangeSystem.indicators.size()];
			for (long processId : result.productIndex.getProcessIds()) {
				double flowResult = result.getSingleFlowResult(processId, flowId);
				if (flowResult == 0d)
					continue;
				int[] values = data.exchangeData.get(new LongPair(processId, flowId));
				for (int i = 0; i < aggregated.length; i++) {
					if (values[i] == 0)
						continue;
					aggregated[i] += values[i] * flowResult;
					divisor[i] += flowResult;
				}
			}
			for (int i = 0; i < aggregated.length; i++) {
				aggregated[i] /= divisor[i];
			}
			flowValues.put(flowId, toIntArray(aggregated));
		}
	}

	private void calculateImpactValues(ContributionResult result, DQData data) {
		for (long impactId : result.impactIndex.getKeys()) {
			double[] aggregated = new double[exchangeSystem.indicators.size()];
			int[] divisor = new int[exchangeSystem.indicators.size()];
			for (long flowId : result.flowIndex.getFlowIds()) {
				int flowIndex = result.flowIndex.getIndex(flowId);
				int impactIndex = result.impactIndex.getIndex(impactId);
				double impactFactor = result.impactFactors.getEntry(impactIndex, flowIndex);
				if (impactFactor == 0d)
					continue;
				int[] values = flowValues.get(flowId);
				for (int i = 0; i < aggregated.length; i++) {
					if (values[i] == 0)
						continue;
					aggregated[i] += values[i] * impactFactor;
					divisor[i] += impactFactor;
				}
			}
			for (int i = 0; i < aggregated.length; i++) {
				aggregated[i] /= divisor[i];
			}
			impactValues.put(impactId, toIntArray(aggregated));
		}
	}

	private void calculateImpactValuesPerProcess(ContributionResult result, DQData data) {
		for (long impactId : result.impactIndex.getKeys()) {
			for (long processId : result.productIndex.getProcessIds()) {
				double[] aggregated = new double[exchangeSystem.indicators.size()];
				int[] divisor = new int[exchangeSystem.indicators.size()];
				for (long flowId : result.flowIndex.getFlowIds()) {
					double flowResult = result.getSingleFlowResult(processId, flowId);
					if (flowResult == 0d)
						continue;
					int flowIndex = result.flowIndex.getIndex(flowId);
					int impactIndex = result.impactIndex.getIndex(impactId);
					double impactFactor = result.impactFactors.getEntry(impactIndex, flowIndex);
					if (impactFactor == 0d)
						continue;
					int[] values = data.exchangeData.get(new LongPair(processId, flowId));
					for (int i = 0; i < aggregated.length; i++) {
						if (values[i] == 0)
							continue;
						aggregated[i] += values[i] * flowResult;
						divisor[i] += flowResult;
					}
				}
				for (int i = 0; i < aggregated.length; i++) {
					aggregated[i] /= divisor[i];
				}
				impactValuesPerProcess.put(new LongPair(processId, impactId), toIntArray(aggregated));
			}
		}
	}

	private int[] toIntArray(double[] dArray) {
		int[] iArray = new int[dArray.length];
		for (int i = 0; i < dArray.length; i++) {
			iArray[i] = (int) Math.round(dArray[i]);
		}
		return iArray;
	}

	private DQResult(DQSystem processSystem, DQSystem exchangeSystem) {
		this.processSystem = processSystem;
		this.exchangeSystem = exchangeSystem;
	}

}
