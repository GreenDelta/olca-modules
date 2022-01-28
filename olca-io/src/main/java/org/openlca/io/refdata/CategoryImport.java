package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.Types;

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
		String refId = Maps.getString(row, 0);
		long id = seq.get(ModelType.CATEGORY, refId);
		String parentRefId = Maps.getString(row, 4);
		Long parentId = parentRefId != null
			? seq.get(ModelType.CATEGORY, parentRefId)
			: null;
		stmt.setLong(1, id); // id
		stmt.setString(2, refId); // refId
		stmt.setString(3, Maps.getString(row, 1)); // name
		stmt.setString(4, Maps.getString(row, 2)); // description
		stmt.setString(5, Maps.getString(row, 3)); // model type
		if (parentId != null)
			stmt.setLong(6, parentId);
		else
			stmt.setNull(6, Types.BIGINT);
	}

}
