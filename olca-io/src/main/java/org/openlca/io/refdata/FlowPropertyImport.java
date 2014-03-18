package org.openlca.io.refdata;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class FlowPropertyImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_flow_properties (id, ref_id, name, "
				+ "f_category, description, flow_property_type, "
				+ "f_unit_group) values (?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		CellProcessor integer = new ParseInt();
		return new CellProcessor[] { notEmpty, // id
				notEmpty, // name
				optional, // description
				optional, // category ID
				notEmpty, // unit group ID
				integer // flow property type
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = Maps.getString(values, 0);
		if (!seq.isInDatabase(ModelType.FLOW_PROPERTY, refId))
			return true;
		log.info("flow property {} {} is already in the database",
				values.get(1), values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = Maps.getString(vals, 0);
		long id = seq.get(ModelType.FLOW_PROPERTY, refId);
		statement.setLong(1, id);
		statement.setString(2, refId);
		statement.setString(3, Maps.getString(vals, 1));
		String catRefId = Maps.getString(vals, 3);
		if (catRefId == null)
			statement.setNull(4, Types.BIGINT);
		else
			statement.setLong(4, seq.get(ModelType.CATEGORY, catRefId));
		statement.setString(5, Maps.getString(vals, 2));
		statement.setInt(6, Maps.getInt(vals, 5));
		statement.setLong(7,
				seq.get(ModelType.UNIT_GROUP, Maps.getString(vals, 4)));
	}
}
