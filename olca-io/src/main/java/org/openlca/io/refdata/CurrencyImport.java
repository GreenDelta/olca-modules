package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.openlca.util.Strings;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class CurrencyImport extends AbstractImport {

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = Maps.getString(values, 0);
		if (!seq.isInDatabase(ModelType.CURRENCY, refId))
			return true;
		log.info("currency {} {} is already in the database",
				Maps.getString(values, 1),
				Maps.getString(values, 0));
		return false;
	}

	@Override
	protected String getStatement() {
		return "insert into tbl_currencies (id, ref_id, name, description, "
				+ "f_category, f_reference_currency, code, conversion_factor) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor required = new StrNotNullOrEmpty();
		CellProcessor number = new ParseDouble();
		CellProcessor optional = new Optional();
		return new CellProcessor[] {
				required, // ID
				required, // name
				optional, // description
				optional, // category ID
				required, // reference currency ID
				required, // currency code
				number // conversion factor
		};
	}

	@Override
	protected void setValues(PreparedStatement stmt, List<Object> vals)
			throws Exception {
		String refId = Maps.getString(vals, 0);
		long id = seq.get(ModelType.CURRENCY, refId);
		stmt.setLong(1, id);
		stmt.setString(2, refId);
		stmt.setString(3, Maps.getString(vals, 1));
		stmt.setString(4, Maps.getString(vals, 2));
		String catId = Maps.getString(vals, 3);
		if (catId == null)
			stmt.setNull(5, Types.BIGINT);
		else
			stmt.setLong(5, seq.get(ModelType.CATEGORY, catId));
		String refCurrencyId = Maps.getString(vals, 4);
		if (Strings.nullOrEqual(refId, refCurrencyId))
			stmt.setLong(6, id);
		else
			stmt.setLong(6, seq.get(ModelType.CURRENCY, refCurrencyId));
		stmt.setString(7, Maps.getString(vals, 5));
		stmt.setDouble(8, Maps.getDouble(vals, 6));
	}

}
