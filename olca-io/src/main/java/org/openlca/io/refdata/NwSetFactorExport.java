package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;

class NwSetFactorExport implements SqlExport {

	@Override
	public String getQuery() {
		return "select nw.ref_id, cat.ref_id, fac.normalisation_factor, "
				+ "fac.weighting_factor from tbl_nw_factors fac "
				+ "inner join tbl_nw_sets nw on  fac.f_nw_set = nw.id "
				+ "inner join tbl_impact_categories cat "
				+ "on fac.f_impact_category = cat.id";
	}

	@Override
	public Object[] createLine(ResultSet rs) throws SQLException {
		var line = new Object[4];
		line[0] = rs.getString(1);
		line[1] = rs.getString(2);
		double n = rs.getDouble(3);
		if (!rs.wasNull())
			line[2] = n;
		double w = rs.getDouble(4);
		line[3] = w;
		return line;
	}
}
