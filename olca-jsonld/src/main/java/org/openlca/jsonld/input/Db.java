package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;

class Db {

	private Map<String, Long> categoryIds = new HashMap<>();
	private Map<String, Long> unitGroupIds = new HashMap<>();
	private Map<String, Long> unitIds = new HashMap<>();

	private IDatabase db;

	public Db(IDatabase db) {
		this.db = db;
	}

	public Category getCategory(String refId) {
		return get(new CategoryDao(db), refId, categoryIds);
	}

	public Category put(Category category) {
		return put(new CategoryDao(db), category, categoryIds);
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

	public UnitGroup getUnitGroup(String refId) {
		return get(new UnitGroupDao(db), refId, unitGroupIds);
	}

	public UnitGroup put(UnitGroup unitGroup) {
		UnitGroup g = put(new UnitGroupDao(db), unitGroup, unitGroupIds);
		if (g == null)
			return null;
		for (Unit unit : g.getUnits())
			unitIds.put(unit.getRefId(), unit.getId());
		return g;
	}

	public Unit getUnit(String refId) {
		RootEntityDao<Unit, BaseDescriptor> dao = new RootEntityDao<>(
				Unit.class, BaseDescriptor.class, db);
		return get(dao, refId, unitIds);
	}

	private <T extends RootEntity> T get(RootEntityDao<T, ?> dao, String refId,
			Map<String, Long> idCache) {
		Long id = idCache.get(refId);
		if (id != null)
			return dao.getForId(id);
		T entity = dao.getForRefId(refId);
		if (entity == null)
			return null;
		idCache.put(refId, entity.getId());
		return entity;
	}

	private <T extends RootEntity> T put(RootEntityDao<T, ?> dao, T entity,
			Map<String, Long> idCache) {
		if (entity == null)
			return null;
		entity = dao.insert(entity);
		idCache.put(entity.getRefId(), entity.getId());
		return entity;
	}

}
