package org.openlca.core.math.data_quality;

import java.util.List;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;

public class DQData {

	/**
	 * Contains the data quality indicators of the processes in a {@code k*n} byte
	 * matrix. The {@code k} data quality indicators of the corresponding data
	 * quality system of the processes are mapped to the rows of this matrix.
	 * The matrix is related to a technosphere index with {@code n} entries which
	 * are mapped to the columns of this matrix.
	 */
	public DenseByteMatrix processData;


	/**
	 * Contains the data quality indicators of the process exchanges. This is
	 * an array of {@code k} byte matrices for each of the {@code k} data quality
	 * indicators of the corresponding data quality system for exchanges that is
	 * applied. Each matrix is then a {@code m*n} byte matrix that contains the
	 * indicator values for the respective data quality indicator for the {@code
	 * m} environmental flows in the {@code n} provider flows.
	 */
	public DenseByteMatrix[] exchangeData;

	public static Builder of(DQSetup setup, IDatabase db) {
			return new Builder(setup, db);
	}

	public static class Builder {

		private final DQSetup setup;
		private final IDatabase db;

		private Builder(DQSetup setup, IDatabase db) {
			this.setup = setup;
			this.db = db;
		}

		public DQData build(TechIndex techIndex, EnviIndex enviIndex) {
			var data = new DQData();
			if (setup == null || db == null || techIndex == null)
				return data;
			if (setup.processSystem != null) {
				data.processData = loadProcessData(techIndex);
			}
			if (setup.exchangeSystem != null && enviIndex != null) {
				data.exchangeData = loadExchangeData(techIndex, enviIndex);
			}
			return data;
		}

		private DenseByteMatrix loadProcessData(TechIndex techIndex) {
			var system = setup.processSystem;
			var k = system.indicators.size();
			var n = techIndex.size();
			var data = new DenseByteMatrix(k, n);

			// query the process table
			var sql = "select id, f_dq_system, dq_entry from tbl_processes";
			NativeSql.on(db).query(sql, r -> {

				// check that we have a valid entry
				long systemId = r.getLong(2);
				if (systemId != system.id)
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
						data.set(row, col, value);
					}
				}
				return true;
			});

			return data;
		}

		private DenseByteMatrix[] loadExchangeData(
				TechIndex techIndex, EnviIndex enviIndex) {

			var system = setup.exchangeSystem;
			if (system == null)
				return null;

			// allocate a BMatrix for each indicator
			var n = system.indicators.size();
			var data = new DenseByteMatrix[n];

			for (int i = 0; i < n; i++) {
				data[i] = new DenseByteMatrix(enviIndex.size(), techIndex.size());
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
				int row = enviIndex.of(flowID, locationID);
				if (row < 0)
					return true;

				// store the values
				int[] values = system.toValues(dqEntry);
				int _n = Math.min(n, values.length);
				for (int i = 0; i < _n; i++) {
					var matrix = data[i];
					byte value = (byte) values[i];
					for (var product : products) {
						int col = techIndex.of(product);
						matrix.set(row, col, value);
					}
				}
				return true;
			});

			return data;
		}
	}
}
