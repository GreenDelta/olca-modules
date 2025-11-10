package org.openlca.core.math.data_quality;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.DQSystem;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Contains the data quality data of a system. In general a data quality system
 * (DQS) has `q` data quality indicators (DQIs). The possible DQI values  range
 * from 1 to the number of scores defined in the respective DQS. A value of 0
 * means that no DQI value is available. All fields in this data structure can
 * be {@code null} when the respective data are not available.
 *
 * @param techDQS  the DQ system of the DQI values that are directly attached
 *                 to the technosphere flows (derived from processes entries)
 *                 of the system.
 * @param techData the DQI values directly attached to technosphere flows
 *                 (derived from processes entries). It is a `q*n` matrix,
 *                 with `q` the number of data quality indicators and `n` the
 *                 number of technosphere flows in the system.
 * @param enviDQS  the DQ system of the DQI values for the intervention flows
 *                 of the system.
 * @param enviData the DQI values for the intervention flows of the system.
 *                 This is an array of `q` matrices for the `q` data quality
 *                 indicators of the respective DQS. Each matrix has a `m*n`
 *                 shape where `m` is the number of intervention flows and `n`
 *                 the number of technosphere flows in the system.
 */
public record DQData(
		DQSystem techDQS,
		DenseByteMatrix techData,
		DQSystem enviDQS,
		DenseByteMatrix[] enviData) {

	public static Builder of(DQSetup setup, IDatabase db) {
		return new Builder(setup, db);
	}

	public boolean hasTechData() {
		return techDQS != null && techData != null;
	}

	public boolean hasEnviData() {
		return enviDQS != null && enviData != null;
	}

	public int techIndicatorCount() {
		return hasTechData()
				? techDQS.indicators.size()
				: 0;
	}

	public int enviIndicatorCount() {
		return hasEnviData()
				? enviDQS.indicators.size()
				: 0;
	}

	public DenseByteMatrix enviData(int i) {
		return enviData != null && enviData.length > i
				? enviData[i]
				: null;
	}

	public static class Builder {

		private final DQSetup setup;
		private final IDatabase db;

		private Builder(DQSetup setup, IDatabase db) {
			this.setup = setup;
			this.db = db;
		}

		public DQData build(TechIndex techIndex, EnviIndex enviIndex) {
			if (setup == null || db == null || techIndex == null)
				return new DQData(null, null, null, null);
			var techData = setup.processSystem != null
					? loadProcessData(techIndex)
					: null;
			var enviData = setup.exchangeSystem != null && enviIndex != null
					? loadExchangeData(techIndex, enviIndex)
					: null;
			return new DQData(
					setup.processSystem, techData, setup.exchangeSystem, enviData);
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
