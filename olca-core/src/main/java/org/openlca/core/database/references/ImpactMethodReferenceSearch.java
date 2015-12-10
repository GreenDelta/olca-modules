package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class ImpactMethodReferenceSearch extends
		BaseReferenceSearch<ImpactMethodDescriptor> {

	private final static Reference[] references = {
		new Reference(ModelType.CATEGORY, "f_category", true),
	};
	private final static Reference[] categoryReferences = {
		new Reference(ModelType.IMPACT_CATEGORY, "id") 
	};
	private final static Reference[] factorReferences = {
		new Reference(ModelType.FLOW, "f_flow"),
		new Reference(ModelType.UNKNOWN, "f_flow_property_factor"),
		new Reference(ModelType.UNIT, "f_unit") 
	};
	
	public ImpactMethodReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<CategorizedDescriptor> results = findReferences(
				"tbl_impact_methods", "id", ids, references);
		List<BaseDescriptor> mixed = findMixedReferences(
				"tbl_impact_categories", "f_impact_method", ids,
				categoryReferences);
		List<ImpactCategoryDescriptor> categories = filterImpactCategories(mixed);
		results.addAll(findFactorReferences(categories));
		results.addAll(findGlobalParameters(ids, getFactorFormulas(ids)));
		return results;
	}

	private List<CategorizedDescriptor> findFactorReferences(
			List<ImpactCategoryDescriptor> categories) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		Set<Long> categoryIds = toIdSet(categories);
		List<BaseDescriptor> mixed = findMixedReferences("tbl_impact_factors",
				"f_impact_category", categoryIds, factorReferences);
		results.addAll(filterCategorized(mixed));
		List<BaseDescriptor> factors = filterUnknown(mixed);
		results.addAll(findFlowProperties(factors));
		List<UnitDescriptor> units = filterUnits(mixed);
		results.addAll(findUnitGroups(units));
		return results;
	}

	private Set<String> getFactorFormulas(Set<Long> ids) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT formula FROM tbl_impact_factors ");
		query.append("INNER JOIN tbl_impact_categories ");
		query.append("ON tbl_impact_categories.id = tbl_impact_factors.f_impact_category ");
		String list = Search.asSqlList(ids.toArray());
		query.append("WHERE f_impact_method IN (" + list + ")");
		Set<String> formulas = new HashSet<>();
		Search.on(database).query(query.toString(), (result) -> {
			formulas.add(result.getString(1));	
		});
		return formulas;
	}
	
}
