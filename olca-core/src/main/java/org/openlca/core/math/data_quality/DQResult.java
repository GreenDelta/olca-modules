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

	public int[] getProcessQuality(long processId) {
		return processValues.get(processId);
	}

	public int[] getFlowQuality(long flowId) {
		return flowValues.get(flowId);
	}

	public static DQResult calculate(IDatabase db, ContributionResult result, long productSystemId) {
		DQData data = DQData.load(db, productSystemId);
		DQResult dqResult = new DQResult(data.processSystem, data.exchangeSystem);
		dqResult.processValues = data.processData;
		dqResult.calculateFlowValues(result, data);
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
