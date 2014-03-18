package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

class ImpactMethodImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_impact_methods (id, ref_id, name, description, "
				+ "f_category) values (?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		return new CellProcessor[] { notEmpty, // id
				notEmpty, // name
				optional, // description
				optional // category ID
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = Maps.getString(values, 0);
		if (!seq.isInDatabase(ModelType.IMPACT_METHOD, refId))
			return true;
		log.info("LCIA method {} {} is already in the database", values.get(1),
				values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = Maps.getString(vals, 0);
		long id = seq.get(ModelType.IMPACT_METHOD, refId);
		statement.setLong(1, id);
		statement.setString(2, refId);
		statement.setString(3, Maps.getString(vals, 1));
		statement.setString(4, Maps.getString(vals, 2));
		String catRefId = Maps.getString(vals, 3);
		if (catRefId == null)
			statement.setNull(5, Types.BIGINT);
		else
			statement.setLong(5, seq.get(ModelType.CATEGORY, catRefId));
	}
}
