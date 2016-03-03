package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.util.Formula;

public class ParameterReferenceSearch extends
		BaseReferenceSearch<ParameterDescriptor> {

	private final static Reference[] references = { 
		new Reference(ModelType.CATEGORY, "f_category", true) 
	};

	public ParameterReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<CategorizedDescriptor> results = new ArrayList<>();
		results.addAll(findReferences("tbl_parameters", "id", ids, references));
		results.addAll(findParameterReferences(ids));
		return results;
	}

	private List<ParameterDescriptor> findParameterReferences(Set<Long> ids) {
		String formulaQuery = createFormulaQuery(ids);
		Set<String> variables = getVariablesUsedInFormulas(formulaQuery);
		String[] names = variables.toArray(new String[variables.size()]);
		return new ParameterDao(database).getDescriptors(names,
				ParameterScope.GLOBAL);
	}

	protected Set<String> getVariablesUsedInFormulas(String formulaQuery) {
		Set<String> formulas = new HashSet<>();
		Search.on(database).query(formulaQuery, (result) -> {
			formulas.add(result.getString(1));
		});
		Set<String> variables = new HashSet<>();
		for (String formula : formulas)
			variables.addAll(Formula.getVariables(formula));
		return variables;
	}
	
	private String createFormulaQuery(Set<Long> ids) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT formula FROM tbl_parameters ");
		query.append("WHERE id IN (" + Search.asSqlList(ids.toArray()) + ")");
		return query.toString();
	}

}
