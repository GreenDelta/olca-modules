package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

class NwSetFactorImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_nw_factors (id, f_nw_set, f_impact_category, "
				+ "normalisation_factor, weighting_factor) values (?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optionalDouble = new Optional(new ParseDouble());
		//@formatter:off
		return new CellProcessor[] {
				notEmpty, // nw-set ID
				notEmpty, // LCIA category ID
				optionalDouble, // nomalisation factor
				optionalDouble // weighting factor
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
		statement.setLong(1, seq.next());
		statement.setLong(2,
				seq.get(ModelType.NW_SET, Maps.getString(values, 0)));
		statement.setLong(3,
				seq.get(ModelType.IMPACT_CATEGORY, Maps.getString(values, 1)));
		Double nf = Maps.getOptionalDouble(values, 2);
		if (nf != null)
			statement.setDouble(4, nf);
		else
			statement.setNull(4, Types.DOUBLE);
		Double wf = Maps.getOptionalDouble(values, 3);
		if (wf != null)
			statement.setDouble(5, wf);
		else
			statement.setNull(5, Types.DOUBLE);
	}
}
