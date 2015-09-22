package org.openlca.jsonld.input;

import java.util.Objects;
import com.google.gson.JsonObject;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CategoryImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private ImportConfig conf;

	private CategoryImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static Category run(String refId, ImportConfig conf) {
		return new CategoryImport(refId, conf).run();
	}

	private Category run() {
		if (refId == null || conf == null)
			return null;
		try {
			Category category = conf.db.getCategory(refId);
			if (category != null)
				return category;
			JsonObject json = conf.store.get(ModelType.CATEGORY, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import category " + refId, e);
			return null;
		}
	}

	private Category map(JsonObject json) {
		if (json == null)
			return null;
		Category category = new Category();
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
