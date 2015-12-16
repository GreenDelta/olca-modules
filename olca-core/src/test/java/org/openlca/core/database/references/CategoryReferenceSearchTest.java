package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryReferenceSearchTest extends BaseReferenceSearchTest {

	private Category category;

	@Override
	public void clear() {
		new CategoryDao(Tests.getDb()).delete(category);
		Tests.clearDb();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.CATEGORY;
	}

	@Override
	protected Category createModel() {
		category = new Category();
		category.setCategory(insertAndAddExpected(new Category()));
		category = Tests.insert(category);
		return category;
	}
}
