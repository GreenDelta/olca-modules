package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;

public class ExchangeReferenceSearch {
	
	private final static Ref[] references = { 
		new Ref(Flow.class, "f_flow"),
		new Ref(FlowPropertyFactor.class, "f_flow_property_factor"),
		new Ref(Unit.class, "f_unit") 
	};

	private final IDatabase database;

	public ExchangeReferenceSearch(IDatabase database) {
		this.database = database;
	}

	public List<Reference> findReferences(Set<Long> ids) {
		return Search.on(database).findReferences("tbl_exchanges", "id", ids,
				references, true);
	}

}
