package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;

class ImpactCategoryImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_impact_categories (id, ref_id, name, "
			+ "description, reference_unit, f_impact_method) "
			+ "values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		var refId = Maps.getString(row, 0);
		return Strings.notEmpty(refId)
			&& !seq.isInDatabase(ModelType.IMPACT_CATEGORY, refId);
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Maps.getString(row, 0);
		long id = seq.get(ModelType.IMPACT_CATEGORY, refId);
		stmt.setLong(1, id);
		stmt.setString(2, refId);
		stmt.setString(3, Maps.getString(row, 1));
		stmt.setString(4, Maps.getString(row, 2));
		stmt.setString(5, Maps.getString(row, 3));
		setRef(stmt, 6, ModelType.IMPACT_METHOD, Maps.getString(row, 4));
	}
}
