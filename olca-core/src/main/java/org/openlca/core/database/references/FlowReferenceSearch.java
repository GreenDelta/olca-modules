package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowReferenceSearch extends
		BaseReferenceSearch<FlowDescriptor> {

	private final static Ref[] references = { 
		new Ref(Category.class, "category", "f_category", true),
		new Ref(Location.class, "location", "f_location", true) 
	};
	private final static Ref[] factorReferences = { 
		new Ref(FlowProperty.class, "flowProperty", FlowPropertyFactor.class, "flowPropertyFactors", "f_flow_property")
	};
	
	public FlowReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, Flow.class, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_flows", "id", ids, references));
		Map<Long, Long> factors = toIdMap(findReferences(
				"tbl_flow_property_factors", "f_flow", ids,
				new Ref[] { new Ref(FlowPropertyFactor.class, "id", "id") }));
		results.addAll(findReferences("tbl_flow_property_factors", "id",
				factors.keySet(), factors, factorReferences));
		return results;
	}

}
