package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;

class NwSetImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_nw_sets (id, ref_id, description, name, "
			+ "f_impact_method, weighted_score_unit) values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Maps.getString(row, 0);
		stmt.setLong(1, seq.get(ModelType.NW_SET, refId));
		stmt.setString(2, refId);
		stmt.setString(3, Maps.getString(row, 2));
		stmt.setString(4, Maps.getString(row, 1));
		setRef(stmt, 5, ModelType.IMPACT_METHOD, Maps.getString(row, 4));
		stmt.setString(6, Maps.getString(row, 3));
	}

}
