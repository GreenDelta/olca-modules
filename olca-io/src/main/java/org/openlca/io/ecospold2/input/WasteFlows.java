package org.openlca.io.ecospold2.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * In ecoinvent 3.x, a waste flow is an intermediate exchange with a negative
 * amount and an opposite direction than the real flow (i.e. it is an output of
 * a waste treatment processes and an input of a waste producing processes).
 * Thus, product flows (intermediate exchanges) that have only negative values
 * can be identified as waste flows (there may also flows with negative and
 * positive values occur as a result of an allocation or system expansion model
 * but it is not that easy to decide whether these are waste flows). This class
 * provides functions to map such negative-only flows to waste flows and waste
 * flows back to negative-only products by switching the amount values and
 * flow directions accordingly.
 */
public class WasteFlows {

	private final IDatabase db;

	private WasteFlows(IDatabase db) {
		this.db = db;
	}

	/**
	 * Maps negative-only product flows to waste flows.
	 */
	public static void map(IDatabase db) {
		WasteFlows w = new WasteFlows(db);
		Set<Long> products = w.getFlows("PRODUCT_FLOW");
		Set<Long> wastes = w.identifyWasteFlows(products);
		w.changeFlowType(wastes, "WASTE_FLOW");
		w.invertExchanges(wastes);
		db.getEntityFactory().getCache().evictAll();
	}

	public static void unmap(IDatabase db) {
		WasteFlows w = new WasteFlows(db);
		Set<Long> wastes = w.getFlows("WASTE_FLOW");
		w.changeFlowType(wastes, "PRODUCT_FLOW");
		w.invertExchanges(wastes);
		db.getEntityFactory().getCache().evictAll();
	}

	/**
	 * Returns the IDs of all flows in the database with the given type.
	 */
	private Set<Long> getFlows(String type) {
		String sql = "SELECT id FROM tbl_flows WHERE flow_type = '" + type + "'";
		HashSet<Long> ids = new HashSet<>();
		try {
			NativeSql.on(db).query(sql, r -> {
				ids.add(r.getLong(1));
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to query product IDs: " + sql, e);
		}
		return ids;
	}

	/**
	 * Returns the IDs of the given product flows that we can identify as waste
	 * flows, i.e. they have always negative values.
	 */
	private Set<Long> identifyWasteFlows(Set<Long> products) {
		String sql = "SELECT f_flow, resulting_amount_value FROM tbl_exchanges";
		HashMap<Long, Boolean> allNegs = new HashMap<>();
		try {
			NativeSql.on(db).query(sql, r -> {
				long flowID = r.getLong(1);
				if (!products.contains(flowID)) {
					return true;
				}
				double amount = r.getDouble(2);
				Boolean prev = allNegs.get(flowID);
				if (prev == null) {
					allNegs.put(flowID, amount < 0);
				} else if (prev && amount >= 0) {
					allNegs.put(flowID, false);
				}
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to search for waste flows: " + sql, e);
		}
		HashSet<Long> wastes = new HashSet<>();
		allNegs.forEach((id, neg) -> {
			if (neg != null && neg) {
				wastes.add(id);
			}
		});
		return wastes;
	}

	private void changeFlowType(Set<Long> flows, String flowType) {
		String sql = "SELECT id, flow_type FROM tbl_flows";
		try (Connection con = db.createConnection();
			 Statement stmt = con.createStatement(
					 ResultSet.TYPE_SCROLL_SENSITIVE,
					 ResultSet.CONCUR_UPDATABLE);
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				long flowID = rs.getLong(1);
				if (!flows.contains(flowID))
					continue;
				rs.updateString(2, flowType);
				rs.updateRow();
			}
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to switch products to waste flows", e);
		}
	}

	private void invertExchanges(Set<Long> wastes) {
		String sql = "SELECT f_flow, " + // 1
				"is_input, " + // 2
				"resulting_amount_value, " +  // 3
				"" +
				"FROM tbl_exchanges";
		try (Connection con = db.createConnection();
			 Statement stmt = con.createStatement(
					 ResultSet.TYPE_SCROLL_SENSITIVE,
					 ResultSet.CONCUR_UPDATABLE);
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				long flowID = rs.getLong(1);
				if (!wastes.contains(flowID))
					continue;
				boolean isInput = rs.getBoolean(2);
				double amount = rs.getDouble(3);
				rs.updateBoolean(2, !isInput);
				rs.updateDouble(3, Math.abs(amount));
				rs.updateRow();
			}
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to swap waste flow exchanges", e);
		}
	}
}
