package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemReferenceSearch extends
		BaseReferenceSearch<ProductSystemDescriptor> {

	private final static Ref[] references = { 
		// don't include reference process, because it is also included in 
		// list of all processes (avoid duplicate reference)
		new Ref(Category.class, "f_category", true),
		new Ref(Exchange.class, "f_reference_exchange"),
		new Ref(FlowPropertyFactor.class, "f_target_flow_property_factor"),
		new Ref(Unit.class, "f_target_unit"),
	};
	private final static Ref[] processReferences = {
		new Ref(Process.class, "f_process")
	};
	private final static Ref[] flowReferences = {
		new Ref(Flow.class, "f_flow")
	};


	public ProductSystemReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_product_systems", "id", ids,
				references));
		results.addAll(findProcesses(ids));
		results.addAll(findFlows(ids));
		results.addAll(findGlobalParameterRedefs(ids));
		return results;
	}
	
	
	private List<Reference> findProcesses(Set<Long> ids) {
		return findReferences("tbl_product_system_processes",
				"f_product_system", ids, processReferences);
	}
	
	private List<Reference> findFlows(Set<Long> ids) {
		return findReferences("tbl_process_links", "f_product_system", ids,
				flowReferences);
	}
	
}
