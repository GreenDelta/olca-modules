package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class FlowPropertyDao extends CategorizedEnitityDao<FlowProperty> {

	public FlowPropertyDao(IDatabase database) {
		super(FlowProperty.class, database);
	}

	public List<BaseDescriptor> whereUsed(FlowProperty prop) {
		return new FlowPropertyUseSearch(getDatabase()).findUses(prop);
	}

}
