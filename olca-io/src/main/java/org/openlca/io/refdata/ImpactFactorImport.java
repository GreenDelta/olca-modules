package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.ModelType;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

class ImpactFactorImport extends Import {

	/**
	 * Maps tupels of (flowId, flowPropertyId) to the IDs of the respective flow
	 * property factors.
	 */
	private HashMap<LongPair, Long> propertyTable;

	@Override
	protected String getStatement() {
		return "insert into tbl_impact_factors (id, f_impact_category, f_flow, "
				+ "f_flow_property_factor, f_unit, value) values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor number = new ParseDouble();
		//@formatter:off
		return new CellProcessor[] { 
				notEmpty, // LCIA category ID
				notEmpty, // flow ID
				notEmpty, // flow property ID
				notEmpty, // unit ID
				number, // factor value
		};
		//@formatter:on
	}

	@Override
	protected boolean isValid(List<Object> values) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> values)
			throws Exception {
		if (propertyTable == null)
			loadPropertyTable();
		long flowId = seq.get(ModelType.FLOW, getString(values, 1));
		long propId = seq.get(ModelType.FLOW_PROPERTY, getString(values, 2));
		long factorId = getFactorId(flowId, propId);
		statement.setLong(1, seq.next());
		statement.setLong(2,
				seq.get(ModelType.IMPACT_CATEGORY, getString(values, 0)));
		statement.setLong(3, flowId);
		statement.setLong(4, factorId);
		statement.setLong(5, seq.get(ModelType.UNIT, getString(values, 3)));
		statement.setDouble(6, getDouble(values, 4));
	}

	private void loadPropertyTable() throws Exception {
		propertyTable = new HashMap<>();
		String query = "select id, f_flow, f_flow_property from "
				+ "tbl_flow_property_factors";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				long factorId = result.getLong(1);
				long flowId = result.getLong(2);
				long propId = result.getLong(3);
				propertyTable.put(LongPair.of(flowId, propId), factorId);
				return true;
			}
		});
	}

	private long getFactorId(long flowId, long propId) {
		Long factorId = propertyTable.get(LongPair.of(flowId, propId));
		return factorId == null ? 0 : factorId;
	}
}
