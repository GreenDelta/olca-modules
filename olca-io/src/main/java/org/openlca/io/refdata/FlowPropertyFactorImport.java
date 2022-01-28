package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;

class FlowPropertyFactorImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_flow_property_factors (id, f_flow, "
			+ "f_flow_property, conversion_factor) values (?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		stmt.setLong(1, seq.next());
		setRef(stmt, 2, ModelType.FLOW, Maps.getString(row, 0));
		setRef(stmt, 3, ModelType.FLOW_PROPERTY, Maps.getString(row, 1));
		stmt.setDouble(4, Maps.getDouble(row, 2));
	}
}
