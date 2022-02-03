package org.openlca.io.refdata;

import java.sql.PreparedStatement;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;

public class CurrencyImport extends AbstractImport {

	@Override
	protected boolean isValid(CSVRecord row) {
		var refId = Maps.getString(row, 0);
		return Strings.notEmpty(refId)
			&& !seq.isInDatabase(ModelType.CURRENCY, refId);
	}

	@Override
	protected String getStatement() {
		return "insert into tbl_currencies (id, ref_id, name, description, "
			+ "f_category, f_reference_currency, code, conversion_factor) "
			+ "values (?, ?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected void setValues(PreparedStatement stmt, CSVRecord row)
		throws Exception {
		String refId = Maps.getString(row, 0);
		long id = seq.get(ModelType.CURRENCY, refId);
		stmt.setLong(1, id);
		stmt.setString(2, refId);
		stmt.setString(3, Maps.getString(row, 1));
		stmt.setString(4, Maps.getString(row, 2));
		setRef(stmt, 5, ModelType.CATEGORY, Maps.getString(row, 3));
		var refCurrencyId = Maps.getString(row, 4);
		if (Strings.nullOrEqual(refId, refCurrencyId)) {
			stmt.setLong(6, id);
		} else {
			setRef(stmt, 6, ModelType.CURRENCY, refCurrencyId);
		}
		stmt.setString(7, Maps.getString(row, 5));
		stmt.setDouble(8, Maps.getDouble(row, 6));
	}

}
