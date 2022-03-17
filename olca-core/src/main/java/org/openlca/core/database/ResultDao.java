package org.openlca.core.database;

import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.ResultDescriptor;

public class ResultDao extends RootEntityDao<Result, ResultDescriptor> {

	public ResultDao(IDatabase db) {
		super(Result.class, ResultDescriptor.class, db);
	}
}
