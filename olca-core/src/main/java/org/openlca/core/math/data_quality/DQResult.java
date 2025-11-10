package org.openlca.core.math.data_quality;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.format.ByteMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.ResultProvider;

/**
 * The data quality result of a system. It provides views to raw DQI values and
 * aggregated DQI values related to different result aspects.
 */
public class DQResult {

	/**
	 * The calculation setup of this data quality result.
	 */
	public final DQSetup setup;

	/**
	 * The LCA result on which this data quality result is based.
	 */
	private final ResultProvider result;

	private final DQData dqData;

	/**
	 * The aggregated DQI values for the inventory results of the system. These
	 * are q vectors for each of the q data quality indicators. Each vector has a
	 * size of m for the m elementary flows in the system. This field is
	 * {@code null} if the system has no inventory result.
	 */
	private ByteMatrix[] enviAgg;

	/**
	 * The aggregated DQI values for the impact results of the system. These are q
	 * vectors for each of the q data quality indicators. Each vector has a size
	 * of k for the k impact categories of the calculation setup. This field is
	 * {@code null} if the system has no impact assessment result.
	 */
	private ByteMatrix[] impactAgg;

	/**
	 * The aggregated DQI values for the impact contributions of the technosphere
	 * flows in the system. These are q matrices for each of the q data quality
	 * indicators. Each matrix has a `k*n` shape for the k impact categories of
	 * the calculation setup and `n` technosphere flows of the system. This field
	 * is {@code null} if the system has no impact assessment result.
	 */
	private ByteMatrix[] techImpactAgg;

	public static DQResult of(IDatabase db, DQSetup setup, ResultProvider result) {
		var data = DQData.of(setup, db).build(
				result.techIndex(), result.enviIndex());
		var r = new DQResult(setup, data, result);
		r.aggregate();
		return r;
	}

	private DQResult(DQSetup setup, DQData data, ResultProvider result) {
		this.setup = setup;
		this.result = result;
		this.dqData = data;
	}

	/**
	 * Get the process-level data quality entry for the given tech.-flow.
	 */
	public int[] get(TechFlow techFlow) {
		if (!dqData.hasTechData())
			return null;
		int j = result.indexOf(techFlow);
		if (j < 0)
			return null;
		int dqiCount = dqData.techIndicatorCount();
		var values = new int[dqiCount];
		for (var dqi = 0; dqi < dqiCount; dqi++) {
			values[dqi] = dqData.techData().get(dqi, j);
		}
		return values;
	}

	/**
	 * Get the raw exchange DQI values for the given tech.- and intervention
	 * flow.
	 */
	public int[] get(TechFlow techFlow, EnviFlow enviFlow) {
		if (!dqData.hasEnviData())
			return null;
		int row = result.indexOf(enviFlow);
		int col = result.indexOf(techFlow);
		if (row < 0 || col < 0)
			return null;
		int dqiCount = dqData.enviIndicatorCount();
		int[] values = new int[dqiCount];
		for (int dqi = 0; dqi < dqiCount; dqi++) {
			var b = dqData.enviData(dqi);
			if (b == null)
				continue;
			values[dqi] = b.get(row, col);
		}
		return values;
	}

	/**
	 * Get the aggregated DQI values for the given intervention flow.
	 */
	public int[] get(EnviFlow flow) {
		if (enviAgg == null)
			return null;
		int row = result.indexOf(flow);
		if (row < 0)
			return null;
		int[] values = new int[enviAgg.length];
		for (var dqi = 0; dqi < enviAgg.length; dqi++) {
			values[dqi] = enviAgg[dqi].get(row, 0);
		}
		return values;
	}

	/**
	 * Get the aggregated DQI values for the given impact category.
	 */
	public int[] get(ImpactDescriptor impact) {
		if (impactAgg == null)
			return null;
		int row = result.indexOf(impact);
		if (row < 0)
			return null;
		int[] values = new int[impactAgg.length];
		for (var dqi = 0; dqi < impactAgg.length; dqi++) {
			values[dqi] = impactAgg[dqi].get(row, 0);
		}
		return values;
	}

	/**
	 * Get the aggregated DQI values for the impact contribution of the given
	 * tech.-flow to the given impact category.
	 */
	public int[] get(ImpactDescriptor impact, TechFlow techFlow) {
		if (techImpactAgg == null)
			return null;
		int row = result.indexOf(impact);
		int col = result.indexOf(techFlow);
		if (row < 0 || col < 0)
			return null;
		int[] values = new int[techImpactAgg.length];
		for (int dqi = 0; dqi < techImpactAgg.length; dqi++) {
			values[dqi] = techImpactAgg[dqi].get(row, col);
		}
		return values;
	}

	private void aggregate() {
		if (setup.aggregationType == AggregationType.NONE
				|| !dqData.hasEnviData())
			return;

		int n = result.techIndex().size();
		int m = result.enviIndex().size();
		int k = result.hasImpacts()
				? result.impactIndex().size()
				: 0;
		boolean hasImpacts = k != 0;
		int q = dqData.enviIndicatorCount();

		var enviAcc = new Accu(setup, m);
		var impactAcc = hasImpacts
				? new Accu(setup, k)
				: null;
		var processAcc = hasImpacts
				? new Accu(setup, k, n)
				: null;

		byte[] dqiValues = new byte[q];
		for (int techFlow = 0; techFlow < n; techFlow++) {
			for (int enviFlow = 0; enviFlow < m; enviFlow++) {

				double enviVal = result.directFlowOf(enviFlow, techFlow);
				if (enviVal == 0)
					continue;

				// fill the DQI values
				for (int dqi = 0; dqi < q; dqi++) {
					var dqMatrix = dqData.enviData(dqi);
					if (dqMatrix == null) {
						dqiValues[dqi] = 0;
						continue;
					}
					dqiValues[dqi] = dqMatrix.get(enviFlow, techFlow);
				}

				// add the intervention DQI values
				enviAcc.add(enviFlow, dqiValues, enviVal);

				// add the impact values
				if (hasImpacts) {
					for (int impact = 0; impact < k; impact++) {
						double factor = result.impactFactorOf(impact, enviFlow);
						if (factor == 0)
							continue;
						double impactVal = factor * enviVal;
						impactAcc.add(impact, dqiValues, impactVal);
						processAcc.add(impact, techFlow, dqiValues, impactVal);
					}
				}
			}
		}

		enviAgg = enviAcc.finish();
		impactAgg = impactAcc != null
				? impactAcc.finish()
				: null;
		techImpactAgg = processAcc != null
				? processAcc.finish()
				: null;
	}
}
