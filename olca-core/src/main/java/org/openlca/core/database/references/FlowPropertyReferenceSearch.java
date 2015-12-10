package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

public class FlowPropertyReferenceSearch extends
		BaseReferenceSearch<FlowPropertyDescriptor> {

	private final static Reference[] references = { 
		new Reference(ModelType.CATEGORY, "f_category", true),
		new Reference(ModelType.UNIT_GROUP, "f_unit_group") 
	};

	public FlowPropertyReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<CategorizedDescriptor> results = findReferences("tbl_flow_properties", "id",
				ids, references);
		return results;
	}

}
