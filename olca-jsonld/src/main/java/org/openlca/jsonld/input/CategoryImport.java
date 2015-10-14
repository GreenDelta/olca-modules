package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

class CategoryImport extends BaseImport<Category> {

	private CategoryImport(String refId, ImportConfig conf) {
		super(ModelType.CATEGORY, refId, conf);
	}

	static Category run(String refId, ImportConfig conf) {
		return new CategoryImport(refId, conf).run();
	}

	@Override
	Category map(JsonObject json, long id) {
		if (json == null)
			return null;
		Category category = new Category();
		category.setId(id);
		In.mapAtts(json, category);
		String typeString = In.getString(json, "modelType");
		if (typeString != null)
			category.setModelType(ModelType.valueOf(typeString));
		String parentId = In.getRefId(json, "category");
		Category parent = CategoryImport.run(parentId, conf);
		if (parent == null)
			return conf.db.put(category);
		else
			return updateParent(parent, category);
	}

	private Category updateParent(Category parent, Category category) {
		category.setCategory(parent);
		parent.getChildCategories().add(category);
		parent = conf.db.updateChilds(parent);
		for (Category child : parent.getChildCategories()) {
			if (Objects.equals(child.getRefId(), category.getRefId()))
				return child;
		}
		return null;
	}

}
