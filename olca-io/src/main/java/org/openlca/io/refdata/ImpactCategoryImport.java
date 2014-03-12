package org.openlca.io.refdata;

import org.openlca.core.model.ModelType;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.sql.PreparedStatement;
import java.util.List;

class ImpactCategoryImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_impact_categories (id, ref_id, name, " +
				"description, reference_unit, f_impact_method) " +
				"values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		return new CellProcessor[]{
				notEmpty, // id
				notEmpty, // name
				optional, // description
				notEmpty, // reference unit
				notEmpty, // method ID
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = getString(values, 0);
		if (!seq.isInDatabase(ModelType.IMPACT_CATEGORY, refId))
			return true;
		log.info("LCIA category {} {} is already in the database", values.get(1),
				values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = getString(vals, 0);
		long id = seq.get(ModelType.IMPACT_CATEGORY, refId);
		statement.setLong(1, id);
		statement.setString(2, refId);
		statement.setString(3, getString(vals, 1));
		statement.setString(4, getString(vals, 2));
		statement.setString(5, getString(vals, 3));
		statement.setLong(6, seq.get(ModelType.IMPACT_METHOD, getString(vals, 4)));
	}
}
