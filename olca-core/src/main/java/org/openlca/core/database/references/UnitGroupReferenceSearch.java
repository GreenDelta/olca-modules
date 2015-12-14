package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupReferenceSearch extends
		BaseReferenceSearch<UnitGroupDescriptor> {

	private final static Reference[] references = { 
		new Reference(ModelType.CATEGORY, "f_category", true),
		new Reference(ModelType.FLOW_PROPERTY, "f_default_flow_property", true) 
	};
	
	public UnitGroupReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		return findReferences("tbl_unit_groups", "id", ids, references);
	}

}
