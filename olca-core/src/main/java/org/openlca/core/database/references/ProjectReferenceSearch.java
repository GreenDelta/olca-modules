package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProjectDescriptor;

public class ProjectReferenceSearch extends
		BaseReferenceSearch<ProjectDescriptor> {

	private final static Ref[] references = { 
		new Ref(Category.class, "category", "f_category", true), 
		new Ref(Actor.class, "author", "f_author", true),	
		new Ref(ImpactMethod.class, "impactMethodId", "f_impact_method", true, true), 			
		new Ref(NwSet.class, "nwSetId", "f_nwset", true, true) 			
	};
	private final static Ref[] variantReferences = { 
		new Ref(ProductSystem.class, "productSystem", ProjectVariant.class, "variants", "f_product_system"), 
		new Ref(FlowPropertyFactor.class, "flowPropertyFactor", ProjectVariant.class, "variants", "f_flow_property_factor"), 
		new Ref(Unit.class, "unit", ProjectVariant.class, "variants", "f_unit")
	};

	public ProjectReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, Project.class, includeOptional);
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
		Map<Long, Long> variants = toIdMap(findReferences(
				"tbl_project_variants", "f_project", ids, new Ref[] { new Ref(
						ProjectVariant.class, "id", "id") }));
		results.addAll(findReferences("tbl_project_variants", "id",
				variants.keySet(), variants, variantReferences));
		results.addAll(findGlobalParameterRedefs(variants.keySet(), variants));
		return results;
	}

}
