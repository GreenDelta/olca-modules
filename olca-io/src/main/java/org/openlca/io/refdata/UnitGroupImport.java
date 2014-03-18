package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

class UnitGroupImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_unit_groups (id, ref_id, name, f_category, "
				+ "description, f_reference_unit, f_default_flow_property) "
				+ "values (?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		return new CellProcessor[] { notEmpty, // 0: refId
				notEmpty, // 1: name
				optional, // 2: description
				optional, // 3: category ID
				optional, // 4: default property ID
				notEmpty, // 5: reference unit ID
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = Maps.getString(values, 0);
		if (!seq.isInDatabase(ModelType.UNIT_GROUP, refId))
			return true;
		log.info("unit group {} {} is already in the database", values.get(1),
				values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = Maps.getString(vals, 0);
		long id = seq.get(ModelType.UNIT_GROUP, refId);
		statement.setLong(1, id);
		statement.setString(2, refId);
		statement.setString(3, Maps.getString(vals, 1));
		String catRefId = Maps.getString(vals, 3);
		if (catRefId == null)
			statement.setNull(4, Types.BIGINT);
		else
			statement.setLong(4, seq.get(ModelType.CATEGORY, catRefId));
		statement.setString(5, Maps.getString(vals, 2));
		statement.setLong(6, seq.get(ModelType.UNIT, Maps.getString(vals, 5)));
		String propRefId = Maps.getString(vals, 4);
		if (propRefId == null)
			statement.setNull(7, Types.BIGINT);
		else
			statement.setLong(7, seq.get(ModelType.FLOW_PROPERTY, propRefId));
	}
}
