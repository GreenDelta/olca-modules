package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

class NwSetImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_nw_sets (id, ref_id, description, name, "
				+ "f_impact_method, weighted_score_unit) values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		//@formatter:off
		return new CellProcessor[] { 
				notEmpty, // id
				notEmpty, // name
				optional, // description
				optional, // weighted score unit
				notEmpty // LCIA method ID
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
		String refId = Maps.getString(values, 0);
		statement.setLong(1, seq.get(ModelType.NW_SET, refId));
		statement.setString(2, refId);
		statement.setString(3, Maps.getString(values, 2));
		statement.setString(4, Maps.getString(values, 1));
		statement.setLong(5,
				seq.get(ModelType.IMPACT_METHOD, Maps.getString(values, 4)));
		statement.setString(6, Maps.getString(values, 3));
	}

}
