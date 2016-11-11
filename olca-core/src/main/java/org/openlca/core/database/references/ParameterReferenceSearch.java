package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.util.Formula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterReferenceSearch extends BaseReferenceSearch<ParameterDescriptor> {

	private final static Logger log = LoggerFactory.getLogger(ParameterReferenceSearch.class);

	private final static Ref[] references = {
			new Ref(Category.class, "category", "f_category", true)
	};

	public ParameterReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, Parameter.class, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_parameters", "id", ids, references));
		results.addAll(findParameterReferences(ids));
		return results;
	}

	private List<Reference> findParameterReferences(Set<Long> ids) {
		List<String> formulaQueries = Search.createQueries("SELECT id, lower(formula) FROM tbl_parameters"
				, "WHERE id IN", ids);
		Map<Long, Set<String>> variables = getVariablesUsedInFormulas(formulaQueries);
		Set<String> names = new HashSet<>();
		for (Set<String> n : variables.values())
			names.addAll(n);
		List<Reference> results = new ArrayList<>();
		List<ParameterDescriptor> descriptors = new ParameterDao(database)
				.getDescriptors(names.toArray(new String[names.size()]),
						ParameterScope.GLOBAL);
		results.addAll(toReferences(descriptors, false, variables, null));
		Set<String> found = new HashSet<>();
		for (ParameterDescriptor d : descriptors)
			found.add(d.getName());
		for (String name : names)
			if (!found.contains(name)) {
				Reference ref = createMissingReference(name, variables);
				if (ref != null)
					results.add(ref);
			}
		return results;
	}

	private Reference createMissingReference(String name,
			Map<Long, Set<String>> ownerToNames) {
		for (long ownerId : ownerToNames.keySet())
			if (ownerToNames.get(ownerId).contains(name))
				return new Reference("", Parameter.class, 0, Parameter.class, ownerId);
		return null;
	}

	protected Map<Long, Set<String>> getVariablesUsedInFormulas(List<String> formulaQueries) {
		Map<Long, Set<String>> variables = new HashMap<>();
		for (String formulaQuery : formulaQueries)
			Search.on(database, null).query(formulaQuery, (result) -> {
				long ownerId = result.getLong(1);
				Set<String> set = variables.get(ownerId);
				if (set == null)
					variables.put(ownerId, set = new HashSet<>());
				try {
					set.addAll(Formula.getVariables(result.getString(2)));
				} catch (Throwable e) {
					log.warn("Failed parsing formula of parameter in model " + ownerId, e);
				}
			});
		return variables;
	}

}
