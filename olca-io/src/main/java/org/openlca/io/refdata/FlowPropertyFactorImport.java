package org.openlca.io.refdata;

import org.openlca.core.model.ModelType;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.sql.PreparedStatement;
import java.util.List;

class FlowPropertyFactorImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_flow_property_factors (id, f_flow, " +
				"f_flow_property, conversion_factor) values (?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor number = new ParseDouble();
		return new CellProcessor[]{
				notEmpty, // flow ID
				notEmpty, // property ID
				number // factor
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> values)
			throws Exception {
		statement.setLong(1, seq.next());
		statement.setLong(2, seq.get(ModelType.FLOW, getString(values, 0)));
		statement.setLong(3, seq.get(ModelType.FLOW_PROPERTY, getString(values, 1)));
		statement.setDouble(4, getDouble(values, 2));
	}
}
