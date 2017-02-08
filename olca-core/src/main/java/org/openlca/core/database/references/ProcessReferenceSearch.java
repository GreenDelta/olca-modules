package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessReferenceSearch extends
		BaseReferenceSearch<ProcessDescriptor> {

	private final static Ref[] references = {
			new Ref(Category.class, "category", "f_category", true),
			new Ref(Location.class, "location", "f_location", true),
			new Ref(Currency.class, "currency", "f_currency", true),
			new Ref(DQSystem.class, "dqSystem", "f_dq_system", true),
			new Ref(DQSystem.class, "exchangeDqSystem", "f_exchange_dq_system", true),
			new Ref(DQSystem.class, "socialDqSystem", "f_social_dq_system", true),
			new Ref(ProcessDocumentation.class, "documentation", "f_process_doc", true)
	};
	private final static Ref[] exchangeReferences = {
			new Ref(Flow.class, "flow", Exchange.class, "exchanges", "f_flow"),
			new Ref(FlowPropertyFactor.class, "flowPropertyFactor", Exchange.class, "exchanges",
					"f_flow_property_factor"),
			new Ref(Unit.class, "unit", Exchange.class, "exchanges", "f_unit"),
			new Ref(Process.class, "defaultProviderId", Exchange.class, "exchanges", "f_default_provider", true, true)
	};
	private final static Ref[] socialAspectReferences = {
			new Ref(SocialIndicator.class, "indicator", SocialAspect.class, "socialAspects", "f_indicator", false),
			new Ref(Source.class, "source", SocialAspect.class, "socialAspects", "f_source", true)
	};
	private final static Ref[] documentationReferences = {
			new Ref(Actor.class, "reviewer", ProcessDocumentation.class, "documentation", "f_reviewer", true),
			new Ref(Actor.class, "dataDocumentor", ProcessDocumentation.class, "documentation", "f_data_documentor",
					true),
			new Ref(Actor.class, "dataGenerator", ProcessDocumentation.class, "documentation", "f_data_generator", true),
			new Ref(Actor.class, "dataSetOwner", ProcessDocumentation.class, "documentation", "f_dataset_owner", true),
			new Ref(Source.class, "publication", ProcessDocumentation.class, "documentation", "f_publication", true)
	};
	private final static Ref[] sourceReferences = {
			new Ref(Source.class, "sources", ProcessDocumentation.class, "documentation", "f_source", true)
	};

	public ProcessReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, Process.class, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> mixed = findReferences("tbl_processes", "id", ids,
				references);
		List<Reference> results = new ArrayList<>();
		results.addAll(filter(CategorizedEntity.class, mixed));
		Map<Long, Long> docIds = toIdMap(filter(ProcessDocumentation.class, mixed));
		results.addAll(findExchangeReferences(ids));
		results.addAll(findSocialAspectReferences(ids));
		results.addAll(findDocumentationReferences(docIds));
		results.addAll(findGlobalParameters(ids, getExchangeFormulas(ids)));
		return results;
	}

	private List<Reference> findExchangeReferences(Set<Long> ids) {
		Map<Long, Long> exchanges = toIdMap(findReferences("tbl_exchanges",
				"f_owner", ids, new Ref[] { new Ref(Exchange.class, "id", "id") }));
		return findReferences("tbl_exchanges", "id", exchanges.keySet(),
				exchanges, exchangeReferences);
	}

	private List<Reference> findSocialAspectReferences(Set<Long> ids) {
		Map<Long, Long> aspects = toIdMap(findReferences("tbl_social_aspects",
				"f_process", ids, new Ref[] { new Ref(Exchange.class, "id", "id") }));
		return findReferences("tbl_social_aspects", "id", aspects.keySet(),
				aspects, socialAspectReferences);
	}

	private List<Reference> findDocumentationReferences(Map<Long, Long> map) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_process_docs", "id", map.keySet(),
				map, documentationReferences));
		results.addAll(findReferences("tbl_process_sources", "f_process_doc",
				map.keySet(), map, sourceReferences));
		return results;
	}

	private Map<Long, Set<String>> getExchangeFormulas(Set<Long> ids) {
		List<String> queries = Search.createQueries(
				"SELECT f_owner, lower(resulting_amount_formula) FROM tbl_exchanges"
				, "WHERE f_owner IN", ids);
		Map<Long, Set<String>> formulas = new HashMap<>();
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
