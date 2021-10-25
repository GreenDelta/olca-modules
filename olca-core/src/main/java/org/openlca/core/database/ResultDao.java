package org.openlca.core.database;

import org.openlca.core.model.ResultModel;
import org.openlca.core.model.descriptors.ResultDescriptor;

public class ResultDao extends CategorizedEntityDao<ResultModel, ResultDescriptor> {

	public ResultDao(IDatabase db) {
		super(ResultModel.class, ResultDescriptor.class, db);
	}
}
