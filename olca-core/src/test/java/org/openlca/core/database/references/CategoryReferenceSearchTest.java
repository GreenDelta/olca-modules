package org.openlca.core.database.references;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.CATEGORY;
	}

	@Override
	protected Category createModel() {
		Category category = new Category();
		category.setCategory(addExpected(new Category()));
		return category;
	}
}
