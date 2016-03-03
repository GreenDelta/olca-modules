package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowReferenceSearch extends
		BaseReferenceSearch<FlowDescriptor> {

	private final static Reference[] references = { 
		new Reference(ModelType.CATEGORY, "f_category", true),
		new Reference(ModelType.LOCATION, "f_location", true) 
	};
	private final static Reference[] factorReferences = { 
		new Reference(ModelType.FLOW_PROPERTY, "f_flow_property")
	};
	
	public FlowReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		results.addAll(findReferences("tbl_flows", "id", ids, references));
		results.addAll(findReferences("tbl_flow_property_factors", "f_flow",
				ids, factorReferences));
		return results;
	}

}
