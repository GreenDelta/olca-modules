package org.openlca.core.database.references;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class ProcessReferenceSearch extends
		BaseReferenceSearch<ProcessDescriptor> {

	private final static Reference[] references = {
		new Reference(ModelType.CATEGORY, "f_category", true),
		new Reference(ModelType.LOCATION, "f_location", true),
		new Reference(ModelType.CURRENCY, "f_currency", true), 
		new Reference(ModelType.UNKNOWN, "f_process_doc", true) 
	};
	private final static Reference[] exchangeReferences = {
		new Reference(ModelType.FLOW, "f_flow"),
		new Reference(ModelType.UNKNOWN, "f_flow_property_factor"),
		new Reference(ModelType.UNIT, "f_unit"), 
		new Reference(ModelType.PROCESS, "f_default_provider") 
	};
	private final static Reference[] socialAspectReferences = { 
		new Reference(ModelType.SOCIAL_INDICATOR, "f_indicator", true), 
		new Reference(ModelType.SOURCE, "f_source", true) 
	};
	private final static Reference[] documentationReferences = {
		new Reference(ModelType.ACTOR, "f_reviewer", true),
		new Reference(ModelType.ACTOR, "f_data_documentor", true),
		new Reference(ModelType.ACTOR, "f_data_generator", true),
		new Reference(ModelType.ACTOR, "f_dataset_owner", true),
		new Reference(ModelType.SOURCE, "f_publication", true) 
	};
	private final static Reference[] sourceReferences = { 
		new Reference(ModelType.SOURCE, "f_source", true) 
	};

	public ProcessReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<BaseDescriptor> mixed = findMixedReferences("tbl_processes", "id",
				ids, references);
		List<CategorizedDescriptor> results = filterCategorized(mixed);
		Set<Long> docIds = toIdSet(filterUnknown(mixed));
		results.addAll(findExchangeReferences(ids));
		results.addAll(findSocialAspectReferences(ids));
		results.addAll(findDocumentationReferences(docIds));
		results.addAll(findGlobalParameters(ids, getExchangeFormulas(ids)));
		return results;
	}

	private List<CategorizedDescriptor> findExchangeReferences(Set<Long> ids) {
		List<BaseDescriptor> mixed = findMixedReferences("tbl_exchanges",
				"f_owner", ids, exchangeReferences);
		List<CategorizedDescriptor> results = filterCategorized(mixed);
		List<BaseDescriptor> factors = filterUnknown(mixed);
		results.addAll(findFlowProperties(factors));
		List<UnitDescriptor> units = filterUnits(mixed);
		results.addAll(findUnitGroups(units));
		return results;
	}

	private List<CategorizedDescriptor> findSocialAspectReferences(Set<Long> ids) {
		return findReferences("tbl_social_aspects", "f_process", ids,
				socialAspectReferences);
	}

	private List<CategorizedDescriptor> findDocumentationReferences(
			Set<Long> ids) {
		List<CategorizedDescriptor> results = findReferences(
				"tbl_process_docs", "id", ids, documentationReferences);
		results.addAll(findReferences("tbl_process_sources", "f_process_doc", ids,
				sourceReferences));
		return results;
	}

	private Set<String> getExchangeFormulas(Set<Long> ids) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT resulting_amount_formula FROM tbl_exchanges ");
		String list = Search.asSqlList(ids.toArray());
		query.append("WHERE f_owner IN (" + list + ")");
		Set<String> f = new HashSet<>();
		String q = query.toString();
		Search.on(database).query(q, (result) -> f.add(result.getString(1)));
		return f;
	}

}
