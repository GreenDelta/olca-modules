package org.openlca.core.matrix.cache;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.CalcAllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AllocationTable {

	private AllocationTable() {
	}

	/**
	 * Get the allocation factors for the given processes from the database.
	 */
	public static List<CalcAllocationFactor> get(IDatabase db, Set<Long> processIDs) {
		String sql = "SELECT allocation_type, f_process, f_product, value, f_exchange "
				+ "FROM tbl_allocation_factors";
		ArrayList<CalcAllocationFactor> factors = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, r -> {
				long processID = r.getLong(2);
				if (!processIDs.contains(processID))
					return true;
				CalcAllocationFactor f = fetch(processID, r);
				if (f != null)
					factors.add(f);
				return true;
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(AllocationTable.class);
			log.error("failed to get allocation factors from database", e);
		}
		return factors;
	}

	private static CalcAllocationFactor fetch(long processID, ResultSet r) {
		CalcAllocationFactor f = new CalcAllocationFactor();
		f.processID = processID;
		try {
			String typeStr = r.getString(1);
			f.method = AllocationMethod.valueOf(typeStr);
			f.flowID = r.getLong(3);
			f.value = r.getDouble(4);
			long exchangeId = r.getLong(5);
			if (!r.wasNull())
				f.exchangeID = exchangeId;
			return f;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(AllocationTable.class);
			log.error("failed to get allocation factor for process " + processID, e);
			return null;
		}
	}
}
