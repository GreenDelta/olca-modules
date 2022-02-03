package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;

class CategoryImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_categories (id, ref_id, name, description, "
			+ "model_type, f_category) values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord values) {
		var refId = Maps.getString(values, 0);
		return Strings.notEmpty(refId)
			&& !seq.isInDatabase(ModelType.CATEGORY, refId);
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		var refId = Maps.getString(row, 0);
		stmt.setLong(1, seq.get(ModelType.CATEGORY, refId)); // id
		stmt.setString(2, refId); // refId
		stmt.setString(3, Maps.getString(row, 1)); // name
		stmt.setString(4, Maps.getString(row, 2)); // description
		stmt.setString(5, Maps.getString(row, 3)); // model type
		setRef(stmt, 6, ModelType.CATEGORY, Maps.getString(row, 4)); // parent
	}

}
