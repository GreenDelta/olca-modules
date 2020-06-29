package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

class ImpactFactorImport extends AbstractImport {

	/**
	 * Maps tupels of (flowId, flowPropertyId) to the IDs of the respective flow
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
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor number = new ParseDouble();
		CellProcessor optional = new Optional();
		return new CellProcessor[] {
				notEmpty, // LCIA category ID
				notEmpty, // flow ID
				notEmpty, // flow property ID
				notEmpty, // unit ID
				number, // factor value
				optional // formula
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement stmt, List<Object> vals)
			throws Exception {
		if (propertyTable == null)
			loadPropertyTable();
		long flowId = seq.get(ModelType.FLOW, Maps.getString(vals, 1));
		long propId = seq.get(ModelType.FLOW_PROPERTY,
				Maps.getString(vals, 2));
		long factorId = getFactorId(flowId, propId);
		stmt.setLong(1, seq.next());
		stmt.setLong(2, seq.get(ModelType.IMPACT_CATEGORY, Maps.getString(vals, 0)));
		stmt.setLong(3, flowId);
		stmt.setLong(4, factorId);
		stmt.setLong(5, seq.get(ModelType.UNIT, Maps.getString(vals, 3)));
		stmt.setDouble(6, Maps.getDouble(vals, 4));
		stmt.setString(7, Maps.getString(vals, 5));
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
