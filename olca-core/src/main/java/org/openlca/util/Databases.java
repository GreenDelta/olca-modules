package org.openlca.util;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.DQSystem;

/**
 * Utility functions for databases.
 */
public class Databases {

	private Databases() {
	}

	public static boolean hasInventoryData(IDatabase db) {
		if (db == null)
			return false;
		var sql = "select count(*) from tbl_exchanges";
		var count = new AtomicInteger(0);
		NativeSql.on(db).query(sql, r -> {
			count.set(r.getInt(1));
			return false;
		});
		return count.get() > 0;
	}

	/**
	 * Returns true when there are characterization factors available in the
	 * database. It does not check if these factors are valid. So this is just a
	 * quick check, e.g for showing the options in a library export of a  database.
	 */
	public static boolean hasImpactData(IDatabase db) {
		if (db == null)
			return false;
		var sql = "select count(*) from tbl_impact_factors";
		var count = new AtomicInteger(0);
		NativeSql.on(db).query(sql, r -> {
			count.set(r.getInt(1));
			return false;
		});
		return count.get() > 0;
	}

	/**
	 * Returns true when the databases has uncertainty information for exchanges
	 * or impact factors.
	 */
	public static boolean hasUncertaintyData(IDatabase db) {
		if (db == null)
			return false;
		var sql = "select distribution_type from tbl_exchanges";
		var b = new AtomicBoolean(false);
		NativeSql.on(db).query(sql, r -> {
			int pos = r.getInt(1);
			if (pos != 0) {
				b.set(true);
				return false;
			}
			return true;
		});
		return b.get();
	}

	/**
	 * Get the common flow data quality system (for exchanges) of all processes
	 * in the databases if such a system exists. This is the case when there is
	 * only one DQ system that is used at least once in all processes of the
	 * database. Otherwise an empty option is returned.
	 */
	public static Optional<DQSystem> getCommonFlowDQS(IDatabase db) {

		// check that there is only one flow DQ system in the processes
		var sql = "select distinct f_exchange_dq_system from tbl_processes";
		var id = new AtomicLong(0L);
		var multiple = new AtomicBoolean(false);
		NativeSql.on(db).query(sql, r -> {
			var next = r.getLong(1);

			// NULL is ok, not every process needs to have DQ values
			if (r.wasNull() || next == 0L)
				return true;

			var current = id.get();
			if (current == next)
				return true;
			if (current == 0L) {
				id.set(next);
				return true;
			}

			multiple.set(true);
			return false;
		});

		// load the DQ system if applicable
		var systemID = id.get();
		if (systemID == 0L || multiple.get())
			return Optional.empty();
		var system = db.get(DQSystem.class, systemID);
		if (system == null)
			return Optional.empty();

		// check that there is at least one DQ entry in the exchanges
		var hasDQ = new AtomicBoolean(false);
		var dqSQL = "select dq_entry from tbl_exchanges";
		NativeSql.on(db).query(dqSQL, r -> {
			var dqe = r.getString(1);
			if (Strings.nullOrEmpty(dqe))
				return true;
			hasDQ.set(true);
			return false;
		});

		return hasDQ.get() ? Optional.of(system) : Optional.empty();
	}
}
