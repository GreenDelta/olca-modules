package org.openlca.core.math.data_quality;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionResult;

public class DQResult {

	public final DQCalculationSetup setup;
	public final DQStatistics statistics;
	private Map<Long, double[]> processValues = new HashMap<>();
	private Map<Long, double[]> flowValues = new HashMap<>();
	private Map<Long, double[]> impactValues = new HashMap<>();
	private Map<LongPair, double[]> flowValuesPerProcess = new HashMap<>();
	private Map<LongPair, double[]> impactValuesPerFlow = new HashMap<>();
	private Map<LongPair, double[]> impactValuesPerProcess = new HashMap<>();

	public double[] get(ProcessDescriptor process) {
		return processValues.get(process.getId());
	}

	public double[] get(FlowDescriptor flow) {
		return flowValues.get(flow.getId());
	}

	public double[] get(ImpactCategoryDescriptor impact) {
		return impactValues.get(impact.getId());
	}

	public double[] get(ProcessDescriptor process, FlowDescriptor flow) {
		return flowValuesPerProcess.get(new LongPair(process.getId(), flow.getId()));
	}

	public double[] get(ProcessDescriptor process, ImpactCategoryDescriptor impact) {
		return impactValuesPerProcess.get(new LongPair(process.getId(), impact.getId()));
	}

	public double[] get(FlowDescriptor flow, ImpactCategoryDescriptor impact) {
		return impactValuesPerFlow.get(new LongPair(flow.getId(), impact.getId()));
	}

	public static DQResult calculate(IDatabase db, ContributionResult result, DQCalculationSetup setup) {
		if (db == null || result == null || setup == null)
			return null;
		if (setup.processDqSystem == null && setup.exchangeDqSystem == null || setup.aggregationType == null
				|| setup.productSystemId == 0l)
			return null;
		DQData data = DQData.load(db, setup, result.flowIndex.getFlowIds());
		DQResult dqResult = new DQResult(setup, data.statistics);
		if (setup.processDqSystem != null) {
			dqResult.processValues = data.processData;
		}
		if (setup.exchangeDqSystem == null)
			return dqResult;
		dqResult.flowValuesPerProcess = data.exchangeData;
		if (setup.aggregationType == AggregationType.NONE)
			return dqResult;
		DQCalculator calculator = new DQCalculator(result, data, setup);
		calculator.calculate();
		dqResult.flowValues = calculator.getFlowValues();
		dqResult.impactValuesPerFlow = calculator.getImpactPerFlowValues();
		dqResult.impactValues = calculator.getImpactValues();
		dqResult.impactValuesPerProcess = calculator.getImpactPerProcessValues();
		return dqResult;
	}

	private DQResult(DQCalculationSetup setup, DQStatistics statistics) {
		this.setup = setup;
		this.statistics = statistics;
	}

}
