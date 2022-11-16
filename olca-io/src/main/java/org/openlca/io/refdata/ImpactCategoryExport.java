package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ImpactCategoryExport implements SqlExport {

	@Override
	public String getQuery() {
		return  "select c.ref_id, c.name, c.description, c.reference_unit, "
				+ "m.ref_id from tbl_impact_categories c join tbl_impact_methods m "
				+ "on c.f_impact_method = m.id";
	}

	@Override
	public Object[] createLine(ResultSet rs) throws SQLException {
		var line = new Object[5];
		line[0] = rs.getString(1);
		line[1] = rs.getString(2);
		line[2] = rs.getString(3);
		line[3] = rs.getString(4);
		line[4] = rs.getString(5);
		return line;
	}
}
