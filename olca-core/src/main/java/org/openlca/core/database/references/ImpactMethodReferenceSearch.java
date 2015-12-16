package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

public class ImpactMethodReferenceSearch extends
		BaseReferenceSearch<ImpactMethodDescriptor> {

	private final static Ref[] references = {
		new Ref(Category.class, "f_category", true),
	};
	private final static Ref[] categoryReferences = {
		new Ref(ImpactCategory.class, "id") 
	};
	private final static Ref[] factorReferences = {
		new Ref(Flow.class, "f_flow"),
		new Ref(FlowPropertyFactor.class, "f_flow_property_factor"),
		new Ref(Unit.class, "f_unit") 
	};
	
	public ImpactMethodReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_impact_methods", "id", ids,
				references));
		List<Reference> mixed = findReferences("tbl_impact_categories",
				"f_impact_method", ids, categoryReferences);
		List<Reference> categories = filter(ImpactCategory.class, mixed);
		results.addAll(findFactorReferences(categories));
		results.addAll(findGlobalParameters(ids, getFactorFormulas(ids)));
		return results;
	}

	private List<Reference> findFactorReferences(
			List<Reference> categories) {
		List<Reference> results = new ArrayList<>();
		Set<Long> categoryIds = toIdSet(categories);
		results.addAll(findReferences("tbl_impact_factors",
				"f_impact_category", categoryIds, factorReferences));
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
