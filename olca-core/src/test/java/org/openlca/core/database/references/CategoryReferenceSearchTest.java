package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryReferenceSearchTest extends BaseReferenceSearchTest {

	private List<Category> categories = new ArrayList<>();

	@Override
	public void clear() {
		for (Category category : categories)
			new CategoryDao(Tests.getDb()).delete(category);
		Tests.clearDb();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.CATEGORY;
	}

	@Override
	protected Category createModel() {
		Category category = new Category();
		category.setCategory(insertAndAddExpected("category", new Category()));
		category = Tests.insert(category);
		categories.add(category);
		return category;
	}
}
