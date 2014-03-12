package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;

class NwSetExport extends AbstractSqlExport {

	@Override
	protected String getQuery() {
		return "select nw.ref_id, nw.name, nw.description, "
				+ "nw.weighted_score_unit, method.ref_id "
				+ "from tbl_nw_sets nw inner join tbl_impact_methods method "
				+ "on nw.f_impact_method = method.id";
	}

	@Override
	protected void logWrittenCount(int count) {
		log.trace("{} nw-sets written", count);
	}

	@Override
	protected Object[] createLine(ResultSet resultSet) throws SQLException {
		Object[] line = new Object[5];
		line[0] = resultSet.getString(1);
		line[1] = resultSet.getString(2);
		line[2] = resultSet.getString(3);
		line[3] = resultSet.getString(4);
		line[4] = resultSet.getString(5);
		return line;
	}

}
