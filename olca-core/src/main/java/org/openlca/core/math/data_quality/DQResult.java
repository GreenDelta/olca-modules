package org.openlca.core.math.data_quality;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.ContributionResult;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Contains the raw data quality data of a setup and result in an efficient
 * data structure.
 */
public class DQResult {

	/**
	 * The calculation setup of this data quality result.
	 */
	public final DQCalculationSetup setup;

	/**
	 * The LCA result on which this data quality result is based.
	 */
	private final ContributionResult result;

	/**
	 * We store the process data in a k*n byte matrix where the k data quality
	 * indicators are mapped to the rows and the n process products to the
	 * columns.
	 */
	private DenseByteMatrix processData;

	/**
	 * For the exchange data we store a flow*product matrix for each
	 * indicator that holds the respective data quality scores of that
	 * indicator.
	 */
	private DenseByteMatrix[] exchangeData;

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
	 * data quality indicators a q*m matrix with the q impact categories
	 * mapped to the rows and the m elementary flows of the setup mapped
	 * to the columns that contains the aggregated data quality values per
	 * impact category and flow for the respective data quality indicator.
	 */
	private DenseByteMatrix[] flowImpactResult;

	/**
	 * If there is an impact result, this field contains for each of the k
	 * data quality indicators a q*n matrix with the q impact categories
	 * mapped to the rows and the n process products of the setup mapped
	 * to the columns that contains the aggregated data quality values per
	 * impact category and process product for the respective data quality
	 * indicator.
	 */
	private DenseByteMatrix[] processImpactResult;

	public static DQResult of(IDatabase db, DQCalculationSetup setup,
							  ContributionResult result) {
		var r = new DQResult(setup, result);
		r.loadProcessData(db);
		r.loadExchangeData(db);
		r.calculateFlowResults();
		r.calculateImpactResults();
		return r;
	}

	private DQResult(DQCalculationSetup setup, ContributionResult result) {
		this.setup = setup;
		this.result = result;
	}

	/**
	 * @deprecated just added for compatibility reasons
	 */
	@Deprecated
	public int[] get(RootDescriptor process) {
		var products = result.techIndex().getProviders(process);
		return products.isEmpty()
			? null
			: get(products.get(0));
	}

	/**
	 * Get the process data quality entry for the given product.
	 */
	public int[] get(TechFlow product) {
		if (processData == null)
			return null;
		int col = result.techIndex().of(product);
		return col < 0
			? null
			: toInt(processData.getColumn(col));
	}

	/**
	 * @deprecated just added for compatibility reasons
	 */
	@Deprecated
	public int[] get(RootDescriptor process, EnviFlow flow) {
		var products = result.techIndex().getProviders(process);
		return products.isEmpty()
			? null
			: get(products.get(0), flow);
	}

	/**
	 * Get the exchange data quality entry for the given product and flow.
	 */
	public int[] get(TechFlow product, EnviFlow flow) {
		if (exchangeData == null)
			return null;
		int row = result.enviIndex().of(flow);
		int col = result.techIndex().of(product);
		if (row < 0 || col < 0)
			return null;
		int[] values = new int[exchangeData.length];
		for (int k = 0; k < exchangeData.length; k++) {
			values[k] = exchangeData[k].get(row, col);
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

	public int[] get(ImpactDescriptor impact, EnviFlow flow) {
		if (flowImpactResult == null)
			return null;
		int row = result.impactIndex().of(impact);
		int col = result.enviIndex().of(flow);
		if (row < 0 || col < 0)
			return null;
		int k = flowImpactResult.length;
		int[] values = new int[k];
		for (int i = 0; i < k; i++) {
			values[i] = flowImpactResult[i].get(row, col);
		}
		return values;
	}

	/**
	 * @deprecated just added for compatibility reasons
	 */
	@Deprecated
	public int[] get(ImpactDescriptor impact, RootDescriptor process) {
		var products = result.techIndex().getProviders(process);
		return products.isEmpty()
			? null
			: get(impact, products.get(0));
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

	private void loadProcessData(IDatabase db) {
		var system = setup.processSystem;
		if (system == null)
			return;

		var k = system.indicators.size();
		var techIndex = result.techIndex();
		var n = techIndex.size();
		processData = new DenseByteMatrix(k, n);

		// query the process table
		var sql = "select id, f_dq_system, dq_entry " +
				  "from tbl_processes";
		NativeSql.on(db).query(sql, r -> {

			// check that we have a valid entry
			long systemID = r.getLong(2);
			if (systemID != system.id)
				return true;
			var dqEntry = r.getString(3);
			if (dqEntry == null)
				return true;
			var providers = techIndex.getProviders(r.getLong(1));
			if (providers.isEmpty())
				return true;

			// store the values of the entry
			int[] values = system.toValues(dqEntry);
			int _k = Math.min(k, values.length);
			for (int row = 0; row < _k; row++) {
				byte value = (byte) values[row];
				for (var provider : providers) {
					int col = techIndex.of(provider);
					processData.set(row, col, value);
				}
			}
			return true;
		});
	}

	private void loadExchangeData(IDatabase db) {
		var system = setup.exchangeSystem;
		var techIndex = result.techIndex();
		var flowIndex = result.enviIndex();
		if (system == null
				|| techIndex == null
				|| flowIndex == null)
			return;

		// allocate a BMatrix for each indicator
		var n = system.indicators.size();
		exchangeData = new DenseByteMatrix[n];

		for (int i = 0; i < n; i++) {
			exchangeData[i] = new DenseByteMatrix(
				flowIndex.size(), techIndex.size());
		}

		// collect the processes (providers) of the result with a
		// matching data quality system
		var providers = new TLongObjectHashMap<List<TechFlow>>();
		var sql = "select id, f_exchange_dq_system from tbl_processes";
		NativeSql.on(db).query(sql, r -> {
			long sysID = r.getLong(2);
			if (sysID != system.id)
				return true;
			var processID = r.getLong(1);
			var products = techIndex.getProviders(processID);
			if (products.isEmpty())
				return true;
			providers.put(processID, products);
			return true;
		});

		// now, scan the exchanges table and collect all
		// matching data quality entries
		sql = "select f_owner, f_flow, f_location, dq_entry from tbl_exchanges";
		NativeSql.on(db).query(sql, r -> {

			// check that we have a valid entry
			var products = providers.get(r.getLong(1));
			if (products == null)
				return true;
			long flowID = r.getLong(2);
			long locationID = r.getLong(3);
			var dqEntry = r.getString(4);
			if (dqEntry == null)
				return true;
			int row = flowIndex.of(flowID, locationID);
			if (row < 0)
				return true;

			// store the values
			int[] values = system.toValues(dqEntry);
			int _n = Math.min(n, values.length);
			for (int i = 0; i < _n; i++) {
				var data = exchangeData[i];
				byte value = (byte) values[i];
				for (var product : products) {
					int col = techIndex.of(product);
					data.set(row, col, value);
				}
			}
			return true;
		});
	}

	/**
	 * Aggregate the raw exchange DQ values with the direct flow contribution
	 * results if applicable.
	 */
	private void calculateFlowResults() {
		if (setup.aggregationType == AggregationType.NONE
			|| exchangeData == null)
			return;

		var system = setup.exchangeSystem;
		int n = result.techIndex().size();
		int k = system.indicators.size();
		int m = result.enviIndex().size();
		flowResult = new DenseByteMatrix(k, m);
		byte max = (byte) system.getScoreCount();

		var acc = new Accumulator(setup, max);
		var flowContributions = new double[n];
		for (int indicator = 0; indicator < k; indicator++) {
			var b = exchangeData[indicator];
			for (int flow = 0; flow < m; flow++) {
				byte[] dqs = b.getRow(flow);
				for (int product = 0; product < n; product++) {
					flowContributions[product] = result.provider()
						.directFlowOf(flow, product);
				}
				flowResult.set(indicator, flow, acc.get(dqs, flowContributions));
			}
		}
	}

	private void calculateImpactResults() {
		if (setup.aggregationType == AggregationType.NONE
			|| exchangeData == null
			|| !result.hasImpacts())
			return;
		if (!result.hasImpacts())
			return;

		// initialize the results
		var provider = result.provider();
		var system = setup.exchangeSystem;
		int k = system.indicators.size();
		int m = result.enviIndex().size();
		int n = result.techIndex().size();
		int q = result.impactIndex().size();
		byte max = (byte) system.getScoreCount();
		impactResult = new DenseByteMatrix(k, q);
		flowImpactResult = new DenseByteMatrix[k];
		processImpactResult = new DenseByteMatrix[k];
		for (int i = 0; i < k; i++) {
			flowImpactResult[i] = new DenseByteMatrix(q, m);
			processImpactResult[i] = new DenseByteMatrix(q, n);
		}

		// initialize the accumulators
		var totalImpactAcc = new Accumulator(setup, max);
		var flowImpactAcc = new Accumulator(setup, max);
		var processAccs = new Accumulator[n];
		for (int j = 0; j < n; j++) {
			processAccs[j] = new Accumulator(setup, max);
		}

		for (int indicator = 0; indicator < k; indicator++) {
			var b = exchangeData[indicator];

			for (int impact = 0; impact < q; impact++) {

				// reset the accumulators
				totalImpactAcc.reset();
				for (var acc : processAccs) {
					acc.reset();
				}

				for (int flow = 0; flow < m; flow++) {

					// get DQ data and calculate weights
					byte[] dqs = b.getRow(flow);
					double factor = provider.impactFactorOf(impact, flow);

					double[] weights = new double[provider.techIndex().size()];
					for (int product = 0; product < weights.length; product++) {
						weights[product] = factor * provider.directFlowOf(
							flow, product);
					}

					// add data
					totalImpactAcc.addAll(dqs, weights);
					flowImpactResult[indicator].set(
						impact, flow, flowImpactAcc.get(dqs, weights));
					for (int process = 0; process < n; process++) {
						processAccs[process].add(dqs[process], weights[process]);
					}

				} // each flow

				impactResult.set(indicator, impact, totalImpactAcc.get());
				for (int process = 0; process < n; process++) {
					processImpactResult[indicator].set(
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
