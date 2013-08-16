package org.openlca.core.matrices;

import java.sql.Connection;
import java.sql.ResultSet;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AllocationTable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProductIndex productIndex;
	private AllocationMethod method;

	public AllocationTable(IDatabase database, ProductIndex productIndex,
			AllocationMethod method) {
		this.productIndex = productIndex;
		this.method = method;
		init(database);
	}

	private void init(IDatabase database) {
		try (Connection con = database.createConnection()) {
			String query = createQuery();
			log.trace("fetch allocation factors: {}", query);
			ResultSet rs = con.createStatement().executeQuery(query);
			while (rs.next())
				fetchFactor(rs);
			rs.close();
		} catch (Exception e) {
			log.error("failed to init allocation table", e);
		}
	}

	private void fetchFactor(ResultSet rs) {
		// TODO Auto-generated method stub

	}

	private String createQuery() {
		String sql = "select * from tbl_allocation_factors where ";
		if (method != AllocationMethod.USE_DEFAULT) {
			// we have do load all factors when the method is USE_DEFAULT,
			// otherwise we can filter the type via the query
			sql += "allocation_type = '" + method.name() + "' AND ";
		}
		sql += "f_process_type in "
				+ Indices.asSql(productIndex.getProcessIds());
		return sql;
	}

	public double getFactor(LongPair processProduct, CalcExchange calcExchange) {
		if (!calcExchange.isInput()
				&& calcExchange.getFlowType() == FlowType.PRODUCT_FLOW)
			return 1d; // TODO: this changes when we allow input-modelling of
						// waste-flows

	}

}
