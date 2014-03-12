package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.supercsv.io.CsvListWriter;

class ImpactFactorExport extends Export {

	@Override
	protected void doIt(final CsvListWriter writer, IDatabase database)
			throws Exception {
		String query = "select cat.ref_id, flow.ref_id, prop.ref_id, unit.ref_id, "
				+ " fac.value from tbl_impact_factors fac "
				+ " inner join tbl_impact_categories cat on fac.f_impact_category = cat.id"
				+ " inner join tbl_flows flow on fac.f_flow = flow.id"
				+ " inner join tbl_flow_property_factors flowfac on fac.f_flow_property_factor = flowfac.id"
				+ " inner join tbl_flow_properties prop on flowfac.f_flow_property = prop.id"
				+ " inner join tbl_units unit on fac.f_unit = unit.id";
		final AtomicInteger count = new AtomicInteger(0);
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet resultSet) throws SQLException {
				return writeLine(resultSet, writer, count);
			}
		});
		log.trace("{} impact factors written", count.get());
	}

	private boolean writeLine(ResultSet resultSet, CsvListWriter writer,
			AtomicInteger count) {
		try {
			Object[] line = createLine(resultSet);
			writer.write(line);
			count.incrementAndGet();
			return true;
		} catch (Exception e) {
			log.error("failed to write line", e);
			return false;
		}
	}

	private Object[] createLine(ResultSet resultSet) throws SQLException {
		Object[] line = new Object[5];
		line[0] = resultSet.getString(1);
		line[1] = resultSet.getString(2);
		line[2] = resultSet.getString(3);
		line[3] = resultSet.getString(4);
		line[4] = resultSet.getDouble(5);
		return line;
	}
}
