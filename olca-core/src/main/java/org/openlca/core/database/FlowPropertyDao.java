package org.openlca.core.database;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

public class FlowPropertyDao extends
        RootEntityDao<FlowProperty, FlowPropertyDescriptor> {

	public FlowPropertyDao(IDatabase database) {
		super(FlowProperty.class, FlowPropertyDescriptor.class, database);
	}

}
