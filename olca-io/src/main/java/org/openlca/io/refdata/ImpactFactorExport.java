package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;

class ImpactFactorExport extends AbstractSqlExport {

	@Override
	protected String getQuery() {
		return "select cat.ref_id, flow.ref_id, prop.ref_id, unit.ref_id, "
				+ " fac.value, fac.formula from tbl_impact_factors fac "
				+ " inner join tbl_impact_categories cat on fac.f_impact_category = cat.id"
				+ " inner join tbl_flows flow on fac.f_flow = flow.id"
				+ " inner join tbl_flow_property_factors flowfac on fac.f_flow_property_factor = flowfac.id"
				+ " inner join tbl_flow_properties prop on flowfac.f_flow_property = prop.id"
				+ " inner join tbl_units unit on fac.f_unit = unit.id";
	}

	@Override
	protected void logWrittenCount(int count) {
		log.trace("{} impact factors written", count);
	}

	@Override
	protected Object[] createLine(ResultSet r) throws SQLException {
		Object[] line = new Object[6];
		line[0] = r.getString(1);
		line[1] = r.getString(2);
		line[2] = r.getString(3);
		line[3] = r.getString(4);
		line[4] = r.getDouble(5);
		line[5] = r.getString(6);
		return line;
	}
}
