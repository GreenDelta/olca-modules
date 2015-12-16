package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessReferenceSearch extends
		BaseReferenceSearch<ProcessDescriptor> {

	private final static Ref[] references = {
		new Ref(Category.class, "f_category", true),
		new Ref(Location.class, "f_location", true),
		new Ref(Currency.class, "f_currency", true), 
		new Ref(ProcessDocumentation.class, "f_process_doc", true) 
	};
	private final static Ref[] exchangeReferences = {
		new Ref(Flow.class, "f_flow"),
		new Ref(FlowPropertyFactor.class, "f_flow_property_factor"),
		new Ref(Unit.class, "f_unit"), 
		new Ref(Process.class, "f_default_provider") 
	};
	private final static Ref[] socialAspectReferences = { 
		new Ref(SocialIndicator.class, "f_indicator", true), 
		new Ref(Source.class, "f_source", true) 
	};
	private final static Ref[] documentationReferences = {
		new Ref(Actor.class, "f_reviewer", true),
		new Ref(Actor.class, "f_data_documentor", true),
		new Ref(Actor.class, "f_data_generator", true),
		new Ref(Actor.class, "f_dataset_owner", true),
		new Ref(Source.class, "f_publication", true) 
	};
	private final static Ref[] sourceReferences = { 
		new Ref(Source.class, "f_source", true) 
	};

	public ProcessReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> mixed = findReferences("tbl_processes", "id", ids,
				references);
		List<Reference> results = new ArrayList<>();
		results.addAll(filter(CategorizedEntity.class, mixed));
		Set<Long> docIds = toIdSet(filter(ProcessDocumentation.class, mixed));
		results.addAll(findExchangeReferences(ids));
		results.addAll(findSocialAspectReferences(ids));
		results.addAll(findDocumentationReferences(docIds));
		results.addAll(findGlobalParameters(ids, getExchangeFormulas(ids)));
		return results;
	}

	private List<Reference> findExchangeReferences(Set<Long> ids) {
		return findReferences("tbl_exchanges", "f_owner", ids,
				exchangeReferences);
	}

	private List<Reference> findSocialAspectReferences(Set<Long> ids) {
		return findReferences("tbl_social_aspects", "f_process", ids,
				socialAspectReferences);
	}

	private List<Reference> findDocumentationReferences(Set<Long> ids) {
		List<Reference> results = new ArrayList<>();
		results.addAll(findReferences("tbl_process_docs", "id", ids,
				documentationReferences));
		results.addAll(findReferences("tbl_process_sources", "f_process_doc",
				ids, sourceReferences));
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
