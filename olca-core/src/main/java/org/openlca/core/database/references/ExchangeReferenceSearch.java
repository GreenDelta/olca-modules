package org.openlca.core.database.references;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;

public class ExchangeReferenceSearch {

	private final static Ref[] references = {
		new Ref(Flow.class, "flow", "f_flow"),
		new Ref(FlowPropertyFactor.class, "flowPropertyFactor", "f_flow_property_factor"),
		new Ref(Unit.class, "unit", "f_unit") 
	};

	private static final Map<Class<? extends AbstractEntity>, Map<Class<? extends AbstractEntity>, String>> nestedProperties = new HashMap<>();

	static {
		putNestedProperty(Process.class, "exchanges");
		putNestedProperty(ProductSystem.class, "referenceExchange");
	}

	private static void putNestedProperty(
			Class<? extends AbstractEntity> ownerType, String nestedProperty) {
		for (Ref ref : references) {
			Map<Class<? extends AbstractEntity>, String> inner = nestedProperties
					.get(ownerType);
			if (inner == null)
				nestedProperties.put(ownerType, inner = new HashMap<>());
			inner.put(ref.type, nestedProperty);
		}
	}

	private final IDatabase database;
	private final Map<Long, Class<? extends AbstractEntity>> ownerTypes;
	private final Map<Long, Long> ownerIds;

	public ExchangeReferenceSearch(IDatabase database) {
		this(database, new HashMap<>(), new HashMap<>());
	}

	public ExchangeReferenceSearch(IDatabase database,
			Map<Long, Class<? extends AbstractEntity>> ownerTypes,
			Map<Long, Long> ownerIds) {
		this.database = database;
		this.ownerTypes = ownerTypes;
		this.ownerIds = ownerIds;
	}

	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> refs = Search.on(database, Exchange.class)
				.findReferences("tbl_exchanges", "id", ids, references, true);
		return Search.applyOwnerMaps(refs, ownerTypes, ownerIds, nestedProperties);
	}
}
