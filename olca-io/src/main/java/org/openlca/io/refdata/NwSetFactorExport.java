package org.openlca.io.refdata;

import java.sql.ResultSet;

class NwSetFactorExport extends AbstractSqlExport {

	@Override
	protected String getQuery() {
		return "select nw.ref_id, cat.ref_id, fac.normalisation_factor, "
				+ "fac.weighting_factor from tbl_nw_factors fac "
				+ "inner join tbl_nw_sets nw on  fac.f_nw_set = nw.id "
				+ "inner join tbl_impact_categories cat "
				+ "on fac.f_impact_category = cat.id";
	}

	@Override
	protected void logWrittenCount(int count) {
		log.trace("{} nw-set factors written", count);
	}

	@Override
	protected Object[] createLine(ResultSet resultSet) throws Exception {
		Object[] line = new Object[4];
		line[0] = resultSet.getString(1);
		line[1] = resultSet.getString(2);
		double n = resultSet.getDouble(3);
		if (!resultSet.wasNull())
			line[2] = n;
		double w = resultSet.getDouble(4);
		line[3] = w;
		return line;
	}
}
