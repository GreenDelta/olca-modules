package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;

class UnitImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_units (id, ref_id, conversion_factor, "
			+ "description, name, synonyms, f_unit_group) values "
			+ "(?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		var refId = Maps.getString(row, 0);
		return Strings.notEmpty(refId)
			&& !seq.isInDatabase(ModelType.UNIT, refId);
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Maps.getString(row, 0);
		long id = seq.get(ModelType.UNIT, refId);
		stmt.setLong(1, id); // id
		stmt.setString(2, refId); // refId
		stmt.setDouble(3, Maps.getDouble(row, 3)); // conversion factor
		stmt.setString(4, Maps.getString(row, 2)); // description
		stmt.setString(5, Maps.getString(row, 1)); // name
		stmt.setString(6, Maps.getString(row, 4)); // synonyms
		setRef(stmt, 7, ModelType.UNIT_GROUP, Maps.getString(row, 5));
	}
}
