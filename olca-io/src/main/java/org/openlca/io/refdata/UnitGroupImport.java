package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;

class UnitGroupImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_unit_groups (id, ref_id, name, f_category, "
			+ "description, f_reference_unit, f_default_flow_property) "
			+ "values (?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		var refId = Maps.getString(row, 0);
		return Strings.notEmpty(refId)
			&& !seq.isInDatabase(ModelType.UNIT_GROUP, refId);
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Maps.getString(row, 0);
		long id = seq.get(ModelType.UNIT_GROUP, refId);
		stmt.setLong(1, id);
		stmt.setString(2, refId);
		stmt.setString(3, Maps.getString(row, 1));
		setRef(stmt, 4, ModelType.CATEGORY, Maps.getString(row, 3));
		stmt.setString(5, Maps.getString(row, 2));
		stmt.setLong(6, seq.get(ModelType.UNIT, Maps.getString(row, 5)));
		setRef(stmt, 7, ModelType.FLOW_PROPERTY, Maps.getString(row, 4));
	}
}
