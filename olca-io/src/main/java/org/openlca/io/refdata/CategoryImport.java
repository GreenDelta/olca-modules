package org.openlca.io.refdata;

import org.openlca.core.model.ModelType;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

class CategoryImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_categories (id, ref_id, name, description, " +
				"model_type, f_parent_category) values (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		return new CellProcessor[]{
				notEmpty, // 0: refId
				notEmpty, // 1: name
				optional, // 2: description
				notEmpty, // 3: model type
				optional, // 4: parent category
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = getString(values, 0);
		if (!seq.isInDatabase(ModelType.CATEGORY, refId))
			return true;
		log.info("category {} {} is already in the database", values.get(1),
				values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = getString(vals, 0);
		long id = seq.get(ModelType.CATEGORY, refId);
		String parentRefId = getString(vals, 4);
		Long parentId = null;
		if (parentRefId != null)
			parentId = seq.get(ModelType.CATEGORY, parentRefId);
		statement.setLong(1, id);                   // id
		statement.setString(2, refId);              // refId
		statement.setString(3, getString(vals, 1)); // name
		statement.setString(4, getString(vals, 2)); // description
		statement.setString(5, getString(vals, 3)); // model type
		if (parentId != null)
			statement.setLong(6, parentId);
		else
			statement.setNull(6, Types.BIGINT);
	}

}
