package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

public class FlowPropertyDao extends
		CategorizedEnitityDao<FlowProperty, FlowPropertyDescriptor> {

	public FlowPropertyDao(IDatabase database) {
		super(FlowProperty.class, FlowPropertyDescriptor.class, database);
	}

	public List<BaseDescriptor> whereUsed(FlowProperty prop) {
		return new FlowPropertyUseSearch(getDatabase()).findUses(prop);
	}

}
