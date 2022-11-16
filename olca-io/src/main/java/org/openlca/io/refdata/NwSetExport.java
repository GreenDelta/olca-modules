package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;

class NwSetExport implements SqlExport {

	@Override
	public String getQuery() {
		return "select nw.ref_id, nw.name, nw.description, "
				+ "nw.weighted_score_unit, method.ref_id "
				+ "from tbl_nw_sets nw inner join tbl_impact_methods method "
				+ "on nw.f_impact_method = method.id";
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
