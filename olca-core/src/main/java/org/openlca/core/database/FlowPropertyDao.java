package org.openlca.core.database;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class FlowPropertyDao extends CategorizedEnitityDao<FlowProperty> {

	public FlowPropertyDao(EntityManagerFactory emf) {
		super(FlowProperty.class, emf);
	}

	public List<BaseDescriptor> whereUsed(FlowProperty prop) {
		return new FlowPropertyUseSearch(getEntityFactory()).findUses(prop);
	}

}
