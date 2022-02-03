package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.Types;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;

class NwSetFactorImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_nw_factors (id, f_nw_set, f_impact_category, "
			+ "normalisation_factor, weighting_factor) values (?, ?, ?, ?, ?)";
	}

	@Override
	protected boolean isValid(CSVRecord row) {
		return true;
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		stmt.setLong(1, seq.next());
		setRef(stmt, 2,ModelType.NW_SET, Maps.getString(row, 0));
		setRef(stmt, 3, ModelType.IMPACT_CATEGORY, Maps.getString(row, 1));
		Double nf = Maps.getOptionalDouble(row, 2);
		if (nf != null)
			stmt.setDouble(4, nf);
		else
			stmt.setNull(4, Types.DOUBLE);
		Double wf = Maps.getOptionalDouble(row, 3);
		if (wf != null)
			stmt.setDouble(5, wf);
		else
			stmt.setNull(5, Types.DOUBLE);
	}
}
