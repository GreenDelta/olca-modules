package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.database.references.Search.Ref;
import org.openlca.core.model.UnitGroup;

public class UnitReferenceSearch {
	
	private final static Ref[] references = { 
		new Ref(UnitGroup.class, "f_unit_group") 
	};

	private final IDatabase database;

	public UnitReferenceSearch(IDatabase database) {
		this.database = database;
	}

	public List<Reference> findReferences(Set<Long> ids) {
		return Search.on(database).findReferences("tbl_units", "id", ids,
				references, true);
	}

}
