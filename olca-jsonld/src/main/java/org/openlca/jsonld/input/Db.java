package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;

class Db {

	private Map<String, Long> categoryIds = new HashMap<>();
	private IDatabase db;

	public Db(IDatabase db) {
		this.db = db;
	}

	public Category getCategory(String refId) {
		CategoryDao dao = new CategoryDao(db);
		Long id = categoryIds.get(refId);
		if (id != null)
			return dao.getForId(id);
		Category category = dao.getForRefId(refId);
		if (category == null)
			return null;
		categoryIds.put(refId, category.getId());
		return category;
	}

	public Category put(Category category) {
		if (category == null)
			return null;
		CategoryDao dao = new CategoryDao(db);
		Category cat = dao.insert(category);
		categoryIds.put(cat.getRefId(), cat.getId());
		return cat;
	}

	public Category updateChilds(Category category) {
		if (category == null)
			return null;
		CategoryDao dao = new CategoryDao(db);
		Category cat = dao.update(category);
		for (Category child : cat.getChildCategories()) {
			String refId = child.getRefId();
			if (categoryIds.containsKey(refId))
				continue;
			categoryIds.put(refId, child.getId());
		}
		return cat;
	}
}
