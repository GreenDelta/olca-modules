package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;

class NwSetImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_nw_sets (id, ref_id, description, name, "
			+ "f_impact_method, weighted_score_unit) values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Csv.get(row, 0);
		stmt.setLong(1, seq.get(NwSet.class, refId));
		stmt.setString(2, refId);
		stmt.setString(3, Csv.get(row, 2));
		stmt.setString(4, Csv.get(row, 1));
		setRef(stmt, 5, ModelType.IMPACT_METHOD, Csv.get(row, 4));
		stmt.setString(6, Csv.get(row, 3));
	}

}
