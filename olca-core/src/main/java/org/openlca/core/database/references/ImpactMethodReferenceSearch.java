package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

public class ImpactMethodReferenceSearch extends
		BaseReferenceSearch<ImpactMethodDescriptor> {

	private final static Ref[] references = {
			new Ref(Category.class, "category", "f_category", true),
	};
	private final static Ref[] categoryReferences = {
			new Ref(ImpactCategory.class, "id", "id")
	};
	private final static Ref[] factorReferences = {
			new Ref(Flow.class, "flow", ImpactFactor.class, "impactFactors", "f_flow"),
			new Ref(FlowPropertyFactor.class, "flowPropertyFactor", ImpactFactor.class, "impactFactors",
					"f_flow_property_factor"),
			new Ref(Unit.class, "unit", ImpactFactor.class, "impactFactors", "f_unit")
	};

	public ImpactMethodReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, ImpactMethod.class, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_impact_methods", "id", ids,
				references));
		results.addAll(findFactorReferences(ids));
		results.addAll(findGlobalParameters(ids, getFactorFormulas(ids)));
		return results;
	}

	private List<Reference> findFactorReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		Map<Long, Long> categories = toIdMap(findReferences(
				"tbl_impact_categories", "f_impact_method", ids,
				categoryReferences));
		Map<Long, Long> factors = toIdMap(findReferences(
				"tbl_impact_factors", "f_impact_category", categories.keySet(),
				new Ref[] { new Ref(ImpactFactor.class, "id", "id") }));
		Map<Long, Long> map = new HashMap<>();
		for (Long factor : factors.keySet())
			map.put(factor, categories.get(factors.get(factor)));
		results.addAll(findReferences("tbl_impact_factors", "id", map.keySet(),
				map, factorReferences));
		return results;
	}

	private Map<Long, Set<String>> getFactorFormulas(Set<Long> ids) {
		String select = "SELECT f_impact_method, formula FROM tbl_impact_factors "
				+ "INNER JOIN tbl_impact_categories "
				+ "ON tbl_impact_categories.id = tbl_impact_factors.f_impact_category ";
		Map<Long, Set<String>> formulas = new HashMap<>();
		List<String> queries = Search.createQueries(select, "WHERE f_impact_method IN", ids);
		for (String query : queries) {
			Search.on(database, null).query(query.toString(), (result) -> {
				long methodId = result.getLong(1);
				Set<String> set = formulas.get(methodId);
				if (set == null)
					formulas.put(methodId, set = new HashSet<>());
				set.add(result.getString(2));
			});
		}
		return formulas;
	}

}
