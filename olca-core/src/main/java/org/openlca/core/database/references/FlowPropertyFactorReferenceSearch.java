package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.FlowProperty;

public class FlowPropertyFactorReferenceSearch {
	
	private final static Ref[] references = { 
		new Ref(FlowProperty.class, "f_flow_property") 
	};

	private final IDatabase database;

	public FlowPropertyFactorReferenceSearch(IDatabase database) {
		this.database = database;
	}

	public List<Reference> findReferences(Set<Long> ids) {
		return Search.on(database).findReferences("tbl_flow_property_factors",
				"id", ids, references, true);
	}

}
