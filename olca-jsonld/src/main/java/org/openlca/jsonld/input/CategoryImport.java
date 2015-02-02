package org.openlca.jsonld.input;

import java.util.Objects;
import com.google.gson.JsonObject;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CategoryImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private EntityStore store;
	private Db db;

	private CategoryImport(String refId, EntityStore store, Db db) {
		this.refId = refId;
		this.store = store;
		this.db = db;
	}

	public static Category run(String refId, EntityStore store, Db db) {
		return new CategoryImport(refId, store, db).run();
	}

	public Category run() {
		if (refId == null || store == null || db == null)
			return null;
		try {
			Category category = db.getCategory(refId);
			if (category != null)
				return category;
			JsonObject json = store.get(ModelType.CATEGORY, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import category " + refId, e);
			return null;
		}
	}

	private Category map(JsonObject json) {
		Category category = new Category();
		In.mapAtts(json, category);
		String typeString = In.getString(json, "modelType");
		if (typeString != null)
			category.setModelType(ModelType.valueOf(typeString));
		String parentId = In.getRefId(json, "parentCategory");
		Category parent = CategoryImport.run(parentId, store, db);
		if (parent == null)
			return db.put(category);
		category.setParentCategory(parent);
		parent.getChildCategories().add(category);
		parent = db.updateChilds(parent);
		for (Category child : parent.getChildCategories()) {
			if (Objects.equals(child.getRefId(), category.getRefId()))
				return child;
		}
		return null;
	}

}
