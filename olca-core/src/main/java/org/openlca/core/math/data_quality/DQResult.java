package org.openlca.core.math.data_quality;

import org.openlca.core.database.IDatabase;
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
	private DenseByteMatrix flowResult;

	/**
	 * A k*q matrix that holds the aggregated impact results for the k
	 * indicators and q impact categories of the setup. It is calculated
	 * by aggregating the exchange data with the direct flow results and
	 * impact factors.
	 */
	private DenseByteMatrix impactResult;

	/**
	 * If there is an impact result, this field contains for each of the k
	 * data quality indicators a q*n matrix with the q impact categories
	 * mapped to the rows and the n process products of the setup mapped
	 * to the columns that contains the aggregated data quality values per
	 * impact category and process product for the respective data quality
	 * indicator.
	 */
	private DenseByteMatrix[] processImpactResult;

	public static DQResult of(IDatabase db, DQSetup setup,
			ResultProvider result) {
		var data = DQData.of(setup, db).build(
				result.techIndex(), result.enviIndex());
		var r = new DQResult(setup, data, result);
		r.calculateFlowResults();
		r.calculateImpactResults();
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
		if (flowResult == null)
			return null;
		int col = result.enviIndex().of(flow);
		return col < 0
				? null
				: toInt(flowResult.getColumn(col));
	}

	/**
	 * Get the aggregated result for the given impact category.
	 */
	public int[] get(ImpactDescriptor impact) {
		if (impactResult == null)
			return null;
		int col = result.impactIndex().of(impact);
		return col < 0
				? null
				: toInt(impactResult.getColumn(col));
	}

	public int[] get(ImpactDescriptor impact, TechFlow product) {
		if (processImpactResult == null)
			return null;
		int row = result.impactIndex().of(impact);
		int col = result.techIndex().of(product);
		if (row < 0 || col < 0)
			return null;
		int k = processImpactResult.length;
		int[] values = new int[k];
		for (int i = 0; i < k; i++) {
			values[i] = processImpactResult[i].get(row, col);
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
					if (dqMatrix == null)
						continue;
					dqiValues[dqi] = dqMatrix.get(enviFlow, techFlow);
				}

				// add the intervention DQI values
				enviAcc.add(enviFlow, dqiValues, enviVal);

				// add the impact values
				if (hasImpacts ) {
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
	}

	/**
	 * Aggregate the raw exchange DQ values with the direct flow contribution
	 * results if applicable.
	 */
	private void calculateFlowResults() {
		if (setup.aggregationType == AggregationType.NONE
				|| !dqData.hasEnviData())
			return;

		var system = setup.exchangeSystem;
		int n = result.techIndex().size();
		int k = dqData.enviIndicatorCount();
		int m = result.enviIndex().size();
		flowResult = new DenseByteMatrix(k, m);
		byte max = (byte) system.getScoreCount();

		var acc = new Accumulator(setup, max);
		var flowContributions = new double[n];
		for (int dqi = 0; dqi < k; dqi++) {
			var b = dqData.enviData(dqi);
			if (b == null)
				continue;
			for (int flow = 0; flow < m; flow++) {
				byte[] dqs = b.getRow(flow);
				for (int product = 0; product < n; product++) {
					flowContributions[product] = result.directFlowOf(flow, product);
				}
				flowResult.set(dqi, flow, acc.get(dqs, flowContributions));
			}
		}
	}

	private void calculateImpactResults() {
		if (setup.aggregationType == AggregationType.NONE
				|| !dqData.hasEnviData()
				|| !result.hasImpacts())
			return;
		if (!result.hasImpacts())
			return;

		// initialize the results
		var system = setup.exchangeSystem;
		int k = dqData.enviIndicatorCount();
		int m = result.enviIndex().size();
		int n = result.techIndex().size();
		int q = result.impactIndex().size();
		byte max = (byte) system.getScoreCount();
		impactResult = new DenseByteMatrix(k, q);
		processImpactResult = new DenseByteMatrix[k];
		for (int i = 0; i < k; i++) {
			processImpactResult[i] = new DenseByteMatrix(q, n);
		}

		// initialize the accumulators
		var totalImpactAcc = new Accumulator(setup, max);
		var processAccs = new Accumulator[n];
		for (int j = 0; j < n; j++) {
			processAccs[j] = new Accumulator(setup, max);
		}

		for (int dqi = 0; dqi < k; dqi++) {
			var b = dqData.enviData(dqi);
			if (b == null)
				continue;

			for (int impact = 0; impact < q; impact++) {

				// reset the accumulators
				totalImpactAcc.reset();
				for (var acc : processAccs) {
					acc.reset();
				}

				for (int flow = 0; flow < m; flow++) {

					// get DQ data and calculate weights
					byte[] dqs = b.getRow(flow);
					double factor = result.impactFactorOf(impact, flow);

					double[] weights = new double[result.techIndex().size()];
					for (int product = 0; product < weights.length; product++) {
						weights[product] = factor * result.directFlowOf(
								flow, product);
					}

					// add data
					totalImpactAcc.addAll(dqs, weights);
					for (int process = 0; process < n; process++) {
						processAccs[process].add(dqs[process], weights[process]);
					}

				} // each flow

				impactResult.set(dqi, impact, totalImpactAcc.get());
				for (int process = 0; process < n; process++) {
					processImpactResult[dqi].set(
							impact, process, processAccs[process].get());
				}
			} // each impact
		} // each indicator
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
