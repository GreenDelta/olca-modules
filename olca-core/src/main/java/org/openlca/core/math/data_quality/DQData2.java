package org.openlca.core.math.data_quality;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.results.BaseResult;

class DQData2 {

	private final DQCalculationSetup setup;
	private final BaseResult result;

	private byte[][] processData;
	private BMatrix[] exchangeData;

	static DQData2 of(IDatabase db, DQCalculationSetup setup, BaseResult result) {
		var data = new DQData2(setup, result);
		data.load(db);
		return data;
	}

	private DQData2(DQCalculationSetup setup, BaseResult result) {
		this.setup = setup;
		this.result = result;
	}

	private void load(IDatabase db) {

		// load the process DQ data
		if (setup.processSystem != null) {

			var system = setup.processSystem;
			var n = system.indicators.size();
			processData = new byte[n][];
			for (int i = 0; i < n; i++) {
				processData[i] = new byte[result.techIndex.size()];
			}

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

		private final int columns;
		private final byte[] data;

		BMatrix(int rows, int columns) {
			this.columns = columns;
			this.data = new byte[rows * columns];
		}

		void set(int row, int col, int value) {
			data[index(row, col)] = (byte) value;
		}

		int get(int row, int col) {
			return data[index(row, col)];
		}

		int index(int row, int column) {
			return column + columns * row;
		}
	}
}
