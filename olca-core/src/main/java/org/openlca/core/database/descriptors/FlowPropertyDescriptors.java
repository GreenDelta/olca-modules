package org.openlca.core.database.descriptors;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

public class FlowPropertyDescriptors
		extends RootDescriptorReader<FlowPropertyDescriptor> {

	private FlowPropertyDescriptors(IDatabase db) {
		super(new FlowPropertyDao(db));
	}

	public static FlowPropertyDescriptors of(IDatabase db) {
		return new FlowPropertyDescriptors(db);
	}
}
