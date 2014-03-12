package org.openlca.io.refdata;

import org.openlca.core.model.ModelType;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

class FlowImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_flows (id, ref_id, name, description, " +
				"f_category, flow_type, cas_number, formula, " +
				"f_reference_flow_property) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		return new CellProcessor[]{
				notEmpty, // id
				notEmpty, // name
				optional, // description
				optional, // category ID
				notEmpty, // flow type
				optional, // CAS
				optional, // formula
				notEmpty, // reference flow property
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = getString(values, 0);
		if (!seq.isInDatabase(ModelType.FLOW, refId))
			return true;
		log.info("flow {} {} is already in the database", values.get(1),
				values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = getString(vals, 0);
		long id = seq.get(ModelType.FLOW, refId);
		statement.setLong(1, id);
		statement.setString(2, refId);
		statement.setString(3, getString(vals, 1));
		statement.setString(4, getString(vals, 2));
		String catRefId = getString(vals, 3);
		if (catRefId == null)
			statement.setNull(5, Types.BIGINT);
		else
			statement.setLong(5, seq.get(ModelType.CATEGORY, catRefId));
		statement.setString(6, getString(vals, 4));
		statement.setString(7, getString(vals, 5));
		statement.setString(8, getString(vals, 6));
		statement.setLong(9, seq.get(ModelType.FLOW_PROPERTY, getString(vals, 7)));
	}
}
