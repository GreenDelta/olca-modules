package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.util.HashMap;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;

class ImpactFactorImport extends AbstractImport {

	/**
	 * Maps tuples of (flowId, flowPropertyId) to the IDs of the respective flow
	 * property factors.
	 */
	private HashMap<LongPair, Long> propertyTable;

	@Override
	protected String getStatement() {
		return "insert into tbl_impact_factors (id, f_impact_category, f_flow, "
			+ "f_flow_property_factor, f_unit, value, formula) "
			+ "values (?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		if (propertyTable == null)
			loadPropertyTable();
		long flowId = seq.get(ModelType.FLOW, Csv.get(row, 1));
		long propId = seq.get(ModelType.FLOW_PROPERTY, Csv.get(row, 2));
		long factorId = getFactorId(flowId, propId);
		stmt.setLong(1, seq.next());
		stmt.setLong(2, seq.get(ModelType.IMPACT_CATEGORY, Csv.get(row, 0)));
		stmt.setLong(3, flowId);
		stmt.setLong(4, factorId);
		stmt.setLong(5, seq.get(Unit.class, Csv.get(row, 3)));
		stmt.setDouble(6, Csv.getDouble(row, 4));
		stmt.setString(7, Csv.get(row, 5));
	}

	private void loadPropertyTable() {
		propertyTable = new HashMap<>();
		String query = "select id, f_flow, f_flow_property from "
			+ "tbl_flow_property_factors";
		NativeSql.on(database).query(query, result -> {
			long factorId = result.getLong(1);
			long flowId = result.getLong(2);
			long propId = result.getLong(3);
			propertyTable.put(LongPair.of(flowId, propId), factorId);
			return true;
		});
	}

	private long getFactorId(long flowId, long propId) {
		Long factorId = propertyTable.get(LongPair.of(flowId, propId));
		return factorId == null ? 0 : factorId;
	}
}
