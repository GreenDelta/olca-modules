package org.openlca.core.math.data_quality;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionResult;

import gnu.trove.set.hash.TLongHashSet;

public class DQResult {

	public final DQCalculationSetup setup;
	public final DQStatistics statistics;
	private Map<Long, int[]> processValues = new HashMap<>();
	private Map<Long, int[]> flowValues = new HashMap<>();
	private Map<Long, int[]> impactValues = new HashMap<>();
	private Map<LongPair, int[]> flowValuesPerProcess = new HashMap<>();
	private Map<LongPair, int[]> impactValuesPerFlow = new HashMap<>();
	private Map<LongPair, int[]> impactValuesPerProcess = new HashMap<>();

	public int[] get(CategorizedDescriptor process) {
		return processValues.get(process.id);
	}

	public int[] get(FlowDescriptor flow) {
		return flowValues.get(flow.id);
	}

	public int[] get(ImpactCategoryDescriptor impact) {
		return impactValues.get(impact.id);
	}

	public int[] get(CategorizedDescriptor process, FlowDescriptor flow) {
		return flowValuesPerProcess
				.get(new LongPair(process.id, flow.id));
	}

	public int[] get(CategorizedDescriptor process,
			ImpactCategoryDescriptor impact) {
		return impactValuesPerProcess
				.get(new LongPair(process.id, impact.id));
	}

	public int[] get(FlowDescriptor flow, ImpactCategoryDescriptor impact) {
		return impactValuesPerFlow
				.get(new LongPair(flow.id, impact.id));
	}

	private DQResult(DQCalculationSetup setup, DQStatistics statistics) {
		this.setup = setup;
		this.statistics = statistics;
	}

	public static DQResult calculate(IDatabase db, ContributionResult result,
			DQCalculationSetup setup) {
		if (db == null || result == null || setup == null)
			return null;
		if (setup.processSystem == null && setup.exchangeSystem == null
				|| setup.aggregationType == null
				|| setup.productSystemId == 0l)
			return null;

		// load the data quality data
		TLongHashSet flowIDs = new TLongHashSet();
		if (result.flowIndex != null) {
			result.flowIndex.each((i, f) -> {
				if (f.flow != null) {
					flowIDs.add(f.flow.id);
				}
			});
		}
		DQData data = DQData.load(db, setup, flowIDs.toArray());

		// create the result
		DQResult dqResult = new DQResult(setup, data.statistics);
		RoundingMode rmode = setup.roundingMode;
		if (setup.processSystem != null) {
			dqResult.processValues = iv(data.processData, rmode);
		}
		if (setup.exchangeSystem == null)
			return dqResult;
		dqResult.flowValuesPerProcess = iv(data.exchangeData, rmode);
		if (setup.aggregationType == AggregationType.NONE)
			return dqResult;
		DQCalculator calc = new DQCalculator(result, data, setup);
		calc.calculate();
		dqResult.flowValues = iv(calc.getFlowValues(), rmode);
		dqResult.impactValuesPerFlow = iv(calc.getImpactPerFlowValues(), rmode);
		dqResult.impactValues = iv(calc.getImpactValues(), rmode);
		dqResult.impactValuesPerProcess = iv(calc.getImpactPerProcessValues(), rmode);
		return dqResult;
	}

	private static <K> Map<K, int[]> iv(Map<K, double[]> m, RoundingMode rmode) {
		Map<K, int[]> im = new HashMap<K, int[]>();
		m.forEach((key, vals) -> {
			if (key == null || vals == null)
				return;
			int[] ivals = new int[vals.length];
			for (int i = 0; i < vals.length; i++) {
				ivals[i] = (int) (rmode == RoundingMode.CEILING
						? Math.ceil(vals[i])
						: Math.round(vals[i]));
			}
			im.put(key, ivals);
		});
		return im;
	}

}
