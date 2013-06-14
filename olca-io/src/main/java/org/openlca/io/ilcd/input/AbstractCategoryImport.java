package org.openlca.io.ilcd.input;

import java.util.List;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;

abstract class AbstractCategoryImport<C> {

	private IDatabase database;
	private Class<?> modelType;

	public AbstractCategoryImport(IDatabase database, Class<?> modelType) {
		this.modelType = modelType;
		this.database = database;
	}

	public Category run(List<C> input) throws ImportException {
		Category rootCategory = selectRootCategory();
		Category category = rootCategory;
		if (input != null && !input.isEmpty()) {
			category = importCategories(rootCategory, input);
		}
		return category;
	}

	private Category selectRootCategory() throws ImportException {
		String className = modelType.getCanonicalName();
		try {
			Category category = database.createDao(Category.class).getForId(
					className);
			return category;
		} catch (Exception e) {
			String message = String.format(
					"Cannot get root category for type %s.", className);
			throw new ImportException(message, e);
		}
	}

	private Category importCategories(Category rootCategory,
			List<C> ilcdCategories) throws ImportException {
		Category nextRootCategory = rootCategory;
		for (C ilcdCategory : ilcdCategories) {
			Category category = findCategoryForIlcdType(nextRootCategory,
					ilcdCategory);
			if (category == null) {
				category = createAndSaveCategory(nextRootCategory, ilcdCategory);
			}
			nextRootCategory = category;
		}
		return nextRootCategory;
	}

	private Category findCategoryForIlcdType(Category rootCategory,
			C ilcdCategory) {
		if (equals(rootCategory, ilcdCategory))
			return rootCategory;
		Category[] categories = rootCategory.getChildCategories();
		Category equalCategory = null;
		for (Category category : categories) {
			if (equals(category, ilcdCategory)) {
				equalCategory = category;
				break;
			}
		}
		return equalCategory;
	}

	private boolean equals(Category category, C ilcdCategory) {
		String name = getName(ilcdCategory);
		return category.getName() != null
				&& category.getName().equalsIgnoreCase(name);
	}

	protected abstract String getName(C ilcdCategory);

	private Category createAndSaveCategory(Category parentCategory,
			C ilcdCategory) throws ImportException {
		try {
			Category newCategory = createNewCategory(ilcdCategory);
			newCategory.setParentCategory(parentCategory);
			parentCategory.add(newCategory);
			database.createDao(Category.class).update(parentCategory);
			return newCategory;
		} catch (Exception e) {
			String message = "Cannot save category in database.";
			throw new ImportException(message, e);
		}
	}

	private Category createNewCategory(C ilcdCategory) {
		Category category = new Category();
		category.setComponentClass(modelType.getCanonicalName());
		category.setId(UUID.randomUUID().toString());
		String name = getName(ilcdCategory);
		category.setName(name);
		return category;
	}
}
