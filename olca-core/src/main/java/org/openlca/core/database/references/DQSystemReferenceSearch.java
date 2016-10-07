package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.DQSystemDescriptor;

public class DQSystemReferenceSearch extends BaseReferenceSearch<DQSystemDescriptor> {

	private final static Ref[] references = { 
		new Ref(Source.class, "source", "f_source", true), 
	};

	public DQSystemReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, DQSystem.class, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		return findReferences("tbl_dq_systems", "id", ids, references);
	}

}
