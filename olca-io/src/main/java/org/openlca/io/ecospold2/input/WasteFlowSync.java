package org.openlca.io.ecospold2.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * In ecoinvent 3.3, waste flows are intermediate exchanges with a negative
 * amount and an opposite direction than the real flow (waste treatment
 * processes have waste as outputs and waste producing processes as inputs
 * in ecoinvent).
 * <p>
 * WasteFlowSync first identifies possible waste flows as product flows that
 * have only negative values in the inputs and outputs. It then changes the
 * flow types of these flows as well as the flow directions and amount values
 * in the exchanges accordingly.
 */
public class WasteFlowSync implements Runnable {

	private final IDatabase db;

	public WasteFlowSync(IDatabase db) {
		this.db = db;
	}

	@Override
	public void run() {
		Set<Long> products = getProducts();
		Set<Long> wastes = getWasteFlows(products);
		updateFlowTypes(wastes);
		updateExchanges(wastes);
	}

	/**
	 * Returns the IDs of all product flows in the database.
	 */
	private Set<Long> getProducts() {
		String sql = "SELECT id FROM tbl_flows WHERE flow_type = 'PRODUCT_FLOW'";
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
	 * Returns the IDs of the given product flows that we can identify as
	 * waste flows, i.e. they have always negative values.
	 */
	private Set<Long> getWasteFlows(Set<Long> products) {
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

	private void updateFlowTypes(Set<Long> wastes) {
		String sql = "SELECT id, flow_type FROM tbl_flows";
		try (Connection con = db.createConnection();
			 Statement stmt = con.createStatement(
					 ResultSet.TYPE_SCROLL_SENSITIVE,
					 ResultSet.CONCUR_UPDATABLE);
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				long flowID = rs.getLong(1);
				if (!wastes.contains(flowID))
					continue;
				rs.updateString(2, "WASTE_FLOW");
				rs.updateRow();
			}
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to switch products to waste flows", e);
		}
	}

	private void updateExchanges(Set<Long> wastes) {
		String sql = "SELECT f_flow, is_input, resulting_amount_value " +
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
