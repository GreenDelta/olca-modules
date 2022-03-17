package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;

class FlowImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_flows (id, ref_id, name, description, "
			+ "f_category, flow_type, cas_number, formula, "
			+ "f_reference_flow_property) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		var refId = Maps.getString(row, 0);
		return Strings.notEmpty(refId)
			&& !seq.isInDatabase(ModelType.FLOW, refId);
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Maps.getString(row, 0);
		long id = seq.get(ModelType.FLOW, refId);
		stmt.setLong(1, id);
		stmt.setString(2, refId);
		stmt.setString(3, Maps.getString(row, 1));
		stmt.setString(4, Maps.getString(row, 2));
		setRef(stmt, 5, ModelType.CATEGORY, Maps.getString(row, 3));
		stmt.setString(6, Maps.getString(row, 4));
		stmt.setString(7, Maps.getString(row, 5));
		stmt.setString(8, Maps.getString(row, 6));
		setRef(stmt, 9, ModelType.FLOW_PROPERTY, Maps.getString(row, 7));
	}
}
