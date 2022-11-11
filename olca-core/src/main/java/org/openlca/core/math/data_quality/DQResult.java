package org.openlca.core.math.data_quality;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.format.ByteMatrix;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import org.openlca.core.results.providers.ResultProvider;

/**
 * Contains the raw data quality data of a setup and result in an efficient
 * data structure.
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
	 * A k*m matrix that holds the aggregated flow results for the k
	 * indicators and m flows of the setup. It is calculated by
	 * aggregating the exchange data with the direct flow contribution
	 * result.
	 */
	private ByteMatrix[] enviAgg;

	/**
	 * A k*q matrix that holds the aggregated impact results for the k
	 * indicators and q impact categories of the setup. It is calculated
	 * by aggregating the exchange data with the direct flow results and
	 * impact factors.
	 */
	private ByteMatrix[] impactAgg;

	/**
	 * If there is an impact result, this field contains for each of the k
	 * data quality indicators a q*n matrix with the q impact categories
	 * mapped to the rows and the n process products of the setup mapped
	 * to the columns that contains the aggregated data quality values per
	 * impact category and process product for the respective data quality
	 * indicator.
	 */
	private ByteMatrix[] processAgg;

	public static DQResult of(IDatabase db, DQSetup setup,
			ResultProvider result) {
		var data = DQData.of(setup, db).build(
				result.techIndex(), result.enviIndex());
		var r = new DQResult(setup, data, result);
		r.aggregate();
		// r.calculateFlowResults();
		// r.calculateImpactResults();
		return r;
	}

	private DQResult(DQSetup setup, DQData data, ResultProvider result) {
		this.setup = setup;
		this.result = result;
		this.dqData = data;
	}

	/**
	 * Get the process data quality entry for the given product.
	 */
	public int[] get(TechFlow product) {
		if (!dqData.hasTechData())
			return null;
		int col = result.techIndex().of(product);
		return col < 0
				? null
				: toInt(dqData.techData().getColumn(col));
	}

	/**
	 * Get the exchange data quality entry for the given product and flow.
	 */
	public int[] get(TechFlow techFlow, EnviFlow enviFlow) {
		if (!dqData.hasEnviData())
			return null;
		int row = result.indexOf(enviFlow);
		int col = result.indexOf(techFlow);
		if (row < 0 || col < 0)
			return null;
		var dqiCount = dqData.enviIndicatorCount();
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
	 * Get the aggregated result for the given flow.
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
	 * Get the aggregated result for the given impact category.
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

	public int[] get(ImpactDescriptor impact, TechFlow product) {
		if (processAgg == null)
			return null;
		int row = result.indexOf(impact);
		int col = result.indexOf(product);
		if (row < 0 || col < 0)
			return null;
		int[] values = new int[processAgg.length];
		for (int dqi = 0; dqi < processAgg.length; dqi++) {
			values[dqi] = processAgg[dqi].get(row, col);
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
		processAgg = processAcc != null
				? processAcc.finish()
				: null;
	}

	private static int[] toInt(byte[] bytes) {
		if (bytes == null)
			return null;
		var ints = new int[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			ints[i] = bytes[i];
		}
		return ints;
	}
}
