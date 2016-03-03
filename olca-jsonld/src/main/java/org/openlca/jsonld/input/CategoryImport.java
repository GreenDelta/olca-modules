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
		Category c = new Category();
		In.mapAtts(json, c, id, conf);
		c.setModelType(In.getEnum(json, "modelType", ModelType.class));
		if (c.getCategory() == null)
			return conf.db.put(c);
		else
			return updateParent(c);
	}

	private Category updateParent(Category category) {
		Category parent = category.getCategory();
		parent.getChildCategories().add(category);
		parent = conf.db.updateChilds(parent);
		for (Category child : parent.getChildCategories()) {
			if (Objects.equals(child.getRefId(), category.getRefId()))
				return child;
		}
		return null;
	}

}
