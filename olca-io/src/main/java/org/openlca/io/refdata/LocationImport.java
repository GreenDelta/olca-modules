package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class LocationImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_locations (id, ref_id, description, name, "
			+ "longitude, latitude, code) values (?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		var refId = Csv.get(row, 0);
		return Strings.notEmpty(refId);
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Csv.get(row, 0);
		long id = seq.get(ModelType.LOCATION, refId);
		stmt.setLong(1, id);
		stmt.setString(2, refId);
		stmt.setString(3, Csv.get(row, 2));
		stmt.setString(4, Csv.get(row, 1));
		stmt.setDouble(5, Csv.getDouble(row, 5));
		stmt.setDouble(6, Csv.getDouble(row, 4));
		stmt.setString(7, Csv.get(row, 3));
	}
}
