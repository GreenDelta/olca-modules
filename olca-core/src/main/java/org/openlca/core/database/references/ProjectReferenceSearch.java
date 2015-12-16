package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProjectDescriptor;

public class ProjectReferenceSearch extends
		BaseReferenceSearch<ProjectDescriptor> {

	private final static Ref[] references = { 
		new Ref(Category.class, "f_category", true), 
		new Ref(Actor.class, "f_author", true),	
		new Ref(ImpactMethod.class, "f_impact_method") 			
	};
	private final static Ref[] variantReferences = { 
		new Ref(ProductSystem.class, "f_product_system"), 
		new Ref(FlowPropertyFactor.class, "f_flow_property_factor"), 
		new Ref(Unit.class, "f_unit")
	};

	public ProjectReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_projects", "id", ids, references));
		results.addAll(findVariantReferences(ids));
		return results;
	}
	
	private List<Reference> findVariantReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_project_variants", "f_project", ids,
				variantReferences));
		results.addAll(findGlobalParameterRedefs(ids));
		return results;
	}

}
