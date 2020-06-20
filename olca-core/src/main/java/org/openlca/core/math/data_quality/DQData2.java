package org.openlca.core.math.data_quality;

import java.util.List;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.results.BaseResult;

class DQData2 {

	private final DQCalculationSetup setup;
	private final BaseResult result;

	/**
	 * We store the process data in a byte matrix where the data quality
	 * indicators are mapped to the rows and the process products to the
	 * columns.
	 */
	private byte[][] processData;

	/**
	 * For the exchange data we store a flow*product matrix for each
	 * indicator that holds the respective data quality scores of that
	 * indicator.
	 */
	private BMatrix[] exchangeData;

	static DQData2 of(IDatabase db, DQCalculationSetup setup, BaseResult result) {
		var data = new DQData2(setup, result);
		data.loadProcessData(db);
		data.loadExchangeData(db);
		return data;
	}

	private DQData2(DQCalculationSetup setup, BaseResult result) {
		this.setup = setup;
		this.result = result;
	}

	private void loadProcessData(IDatabase db) {
		var system = setup.processSystem;
		if (system == null)
			return;

		var n = system.indicators.size();
		processData = new byte[n][];
		for (int i = 0; i < n; i++) {
			processData[i] = new byte[result.techIndex.size()];
		}

		// query the process table
		var techIndex = result.techIndex;
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
			int _n = Math.min(n, values.length);
			for (int i = 0; i < _n; i++) {
				byte[] data = processData[i];
				byte value = (byte) values[i];
				for (var provider : providers) {
					int col = techIndex.getIndex(provider);
					data[col] = value;
				}
			}
			return true;
		});
	}

	private void loadExchangeData(IDatabase db) {
		var system = setup.exchangeSystem;
		if (system == null || result.flowIndex == null)
			return;

		// allocate a BMatrix for each indicator
		var n = system.indicators.size();
		exchangeData = new BMatrix[n];
		var techIndex = result.techIndex;
		var flowIndex = result.flowIndex;
		for (int i = 0; i < n; i++) {
			exchangeData[i] = new BMatrix(
					flowIndex.size(), techIndex.size());
		}

		// collect the processes (providers) of the result with a
		// matching data quality system
		var providers = new TLongObjectHashMap<List<ProcessProduct>>();
		var sql = "select id, f_exchange_dq_system from tbl_processes";
		NativeSql.on(db).query(sql, r -> {
			long sysID = r.getLong(2);
			if (sysID != system.id)
				return true;
			var processID = r.getLong(1);
			var products = result.techIndex.getProviders(processID);
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
					int col = techIndex.getIndex(product);
					data.set(row, col, value);
				}
			}
			return true;
		});
	}


	/**
	 * Get the process data quality entry for the given product.
	 */
	public int[] get(ProcessProduct product) {
		if (processData == null)
			return null;
		int col = result.techIndex.getIndex(product);
		if (col < 0)
			return null;
		int[] values = new int[processData.length];
		for (int row = 0; row < processData.length; row++) {
			values[row] = processData[row][col];
		}
		return values;
	}

	/**
	 * Get the exchange data quality entry for the given product and flow.
	 */
	public int[] get(ProcessProduct product, IndexFlow flow) {
		if (exchangeData == null)
			return null;
		int row = result.flowIndex.of(flow);
		int col = result.techIndex.getIndex(product);
		if (row < 0 || col < 0)
			return null;
		int[] values = new int[exchangeData.length];
		for (int k = 0; k < exchangeData.length; k++) {
			values[k] = exchangeData[k].get(row, col);
		}
		return values;
	}

	private static class BMatrix {

		private final int rows;
		private final byte[] data;

		BMatrix(int rows, int columns) {
			this.rows = rows;
			this.data = new byte[rows * columns];
		}

		void set(int row, int col, int value) {
			data[index(row, col)] = (byte) value;
		}

		int get(int row, int col) {
			return data[index(row, col)];
		}

		int index(int row, int column) {
			return row + rows * column;
		}
	}
}
