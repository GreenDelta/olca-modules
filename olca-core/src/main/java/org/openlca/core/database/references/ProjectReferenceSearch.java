package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class ProjectReferenceSearch extends
		BaseReferenceSearch<ProjectDescriptor> {

	private final static Reference[] references = { 
		new Reference(ModelType.CATEGORY, "f_category", true), 
		new Reference(ModelType.ACTOR, "f_author", true),	
		new Reference(ModelType.IMPACT_METHOD, "f_impact_method") 			
	};
	private final static Reference[] variantReferences = { 
		new Reference(ModelType.PRODUCT_SYSTEM, "f_product_system"), 
		new Reference(ModelType.UNKNOWN, "f_flow_property_factor"), 
		new Reference(ModelType.UNIT, "f_unit")
	};

	public ProjectReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		results.addAll(findReferences("tbl_projects", "id", ids, references));
		results.addAll(findVariantReferences(ids));
		return results;
	}
	
	private List<CategorizedDescriptor> findVariantReferences(Set<Long> ids) {
		List<BaseDescriptor> mixed = findMixedReferences(
				"tbl_project_variants", "f_project", ids, variantReferences);
		List<CategorizedDescriptor> results = filterCategorized(mixed);
		List<BaseDescriptor> factors = filterUnknown(mixed);
		results.addAll(findFlowProperties(factors));
		List<UnitDescriptor> units = filterUnits(mixed);
		results.addAll(findUnitGroups(units));
		results.addAll(findGlobalParameterRedefs(ids));
		return results;
	}

}
