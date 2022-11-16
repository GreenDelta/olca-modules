package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.LoggerFactory;

public class ImpactCategoryExport implements Export {

	@Override
	public void doIt(CSVPrinter printer, IDatabase db) {
		String query = "select c.ref_id, c.name, c.description, c.reference_unit, "
				+ "m.ref_id from tbl_impact_categories c join tbl_impact_methods m "
				+ "on c.f_impact_method = m.id";
		var count = new AtomicInteger(0);
		NativeSql.on(db).query(query, r -> {
			try {
				Object[] line = createLine(r);
				printer.printRecord(line);
				count.incrementAndGet();
				return true;
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("failed to write line", e);
				return false;
			}
		});
	}

	private Object[] createLine(ResultSet rs) throws SQLException {
		Object[] line = new Object[5];
		line[0] = rs.getString(1);
		line[1] = rs.getString(2);
		line[2] = rs.getString(3);
		line[3] = rs.getString(4);
		line[4] = rs.getString(5);
		return line;
	}
}
