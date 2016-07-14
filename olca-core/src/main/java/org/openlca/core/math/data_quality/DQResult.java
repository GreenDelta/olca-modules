package org.openlca.core.math.data_quality;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionResult;

public class DQResult {

	public final DQSystem processSystem;
	public final DQSystem exchangeSystem;
	public final AggregationType aggregationType;
	private Map<Long, int[]> processValues = new HashMap<>();
	private Map<Long, int[]> flowValues = new HashMap<>();
	private Map<Long, int[]> impactValues = new HashMap<>();
	private Map<LongPair, int[]> flowValuesPerProcess = new HashMap<>();
	private Map<LongPair, int[]> impactValuesPerFlow = new HashMap<>();
	private Map<LongPair, int[]> impactValuesPerProcess = new HashMap<>();

	public int[] get(ProcessDescriptor process) {
		return processValues.get(process.getId());
	}

	public int[] get(FlowDescriptor flow) {
		return flowValues.get(flow.getId());
	}

	public int[] get(ImpactCategoryDescriptor impact) {
		return impactValues.get(impact.getId());
	}

	public int[] get(ProcessDescriptor process, FlowDescriptor flow) {
		return flowValuesPerProcess.get(new LongPair(process.getId(), flow.getId()));
	}

	public int[] get(ProcessDescriptor process, ImpactCategoryDescriptor impact) {
		return impactValuesPerProcess.get(new LongPair(process.getId(), impact.getId()));
	}

	public int[] get(FlowDescriptor flow, ImpactCategoryDescriptor impact) {
		return impactValuesPerFlow.get(new LongPair(flow.getId(), impact.getId()));
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
		DQCalculator calculator = new DQCalculator(result, data);
		calculator.calculate();
		dqResult.flowValues = calculator.getFlowValues(type);
		dqResult.impactValuesPerFlow = calculator.getImpactPerFlowValues(type);
		dqResult.impactValues = calculator.getImpactValues(type);
		dqResult.impactValuesPerProcess = calculator.getImpactPerProcessValues(type);
		return dqResult;
	}

	private DQResult(DQSystem processSystem, DQSystem exchangeSystem, AggregationType type) {
		this.processSystem = processSystem;
		this.exchangeSystem = exchangeSystem;
		this.aggregationType = type;
	}

}
