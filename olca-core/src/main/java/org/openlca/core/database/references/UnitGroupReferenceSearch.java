package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupReferenceSearch extends
		BaseReferenceSearch<UnitGroupDescriptor> {

	private final static Ref[] references = { 
		new Ref(Category.class, "f_category", true),
		new Ref(FlowProperty.class, "f_default_flow_property", true) 
	};
	
	public UnitGroupReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		return findReferences("tbl_unit_groups", "id", ids, references);
	}

}
