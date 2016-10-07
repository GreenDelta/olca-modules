package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.CategoryDescriptor;

public class CategoryReferenceSearch extends
		BaseReferenceSearch<CategoryDescriptor> {

	private final static Ref[] references = { 
		new Ref(Category.class, "category", "f_category", true) 
	};

	public CategoryReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, Category.class, includeOptional);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		return findReferences("tbl_categories", "id", ids, references);
	}

}
