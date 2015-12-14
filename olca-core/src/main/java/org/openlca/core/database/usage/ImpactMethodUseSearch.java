package org.openlca.core.database.usage;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

/**
 * Searches for the use of impact methods in other entities. Impact methods can
 * be used in projects.
 */
public class ImpactMethodUseSearch extends
		BaseUseSearch<ImpactMethodDescriptor> {

	public ImpactMethodUseSearch(IDatabase database) {
		super(database);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		return queryFor(ModelType.PROJECT, ids, "f_impact_method");
	}

}
