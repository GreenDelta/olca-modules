package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ResultDao;
import org.openlca.core.model.descriptors.ResultDescriptor;

public class ResultDescriptors
		extends RootDescriptorReader<ResultDescriptor> {

	private ResultDescriptors(IDatabase db) {
		super(new ResultDao(db));
	}

	public static ResultDescriptors of(IDatabase db) {
		return new ResultDescriptors(db);
	}
}
