package org.openlca.io.refdata;

import org.openlca.core.model.ModelType;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.sql.PreparedStatement;
import java.util.List;

class UnitImport extends Import {

	@Override
	protected String getStatement() {
		return "insert into tbl_units (id, ref_id, conversion_factor, " +
				"description, name, synonyms, f_unit_group) values " +
				"(?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		CellProcessor number = new ParseDouble();
		return new CellProcessor[]{
				notEmpty, // id
				notEmpty, // name
				optional, // description
				number, // conversion factor
				optional, // synonyms
				notEmpty // unit group
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = getString(values, 0);
		if (!seq.isInDatabase(ModelType.UNIT, refId))
			return true;
		log.info("unit {} {} is already in the database", values.get(1),
				values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = getString(vals, 0);
		long id = seq.get(ModelType.UNIT, refId);
		statement.setLong(1, id);                   // id
		statement.setString(2, refId);              // refId
		statement.setDouble(3, getDouble(vals, 3)); // conversion factor
		statement.setString(4, getString(vals, 2)); // description
		statement.setString(5, getString(vals, 1)); // name
		statement.setString(6, getString(vals, 4)); // synonyms
		long groupId = seq.get(ModelType.UNIT_GROUP, getString(vals, 5));
		statement.setLong(7, groupId);
	}
}
