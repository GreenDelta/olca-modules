package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;

public class FlowPropertyImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_flow_properties (id, ref_id, name, "
			+ "f_category, description, flow_property_type, "
			+ "f_unit_group) values (?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		var refId = Maps.getString(row, 0);
		return Strings.notEmpty(refId)
			&& !seq.isInDatabase(ModelType.FLOW_PROPERTY, refId);
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Maps.getString(row, 0);
		long id = seq.get(ModelType.FLOW_PROPERTY, refId);
		stmt.setLong(1, id);
		stmt.setString(2, refId);
		stmt.setString(3, Maps.getString(row, 1));
		setRef(stmt, 4, ModelType.CATEGORY, Maps.getString(row, 3));
		stmt.setString(5, Maps.getString(row, 2));
		stmt.setInt(6, Maps.getInt(row, 5));
		setRef(stmt, 7, ModelType.UNIT_GROUP, Maps.getString(row, 4));
	}
}
