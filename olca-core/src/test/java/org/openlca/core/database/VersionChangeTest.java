package org.openlca.core.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;

public class VersionChangeTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testUpdate() throws Exception {
		for (var type : ModelType.values()) {
			if (!type.isRoot())
				continue;
			var entity = (RootEntity) type.getModelClass()
					.getDeclaredConstructor()
					.newInstance();
			testUpdate(entity);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends RootEntity> void testUpdate(T instance) {
		var dao = (BaseDao<T>) Daos.base(db, instance.getClass());
		instance = dao.insert(instance);
		assertEquals(0L, instance.version);
		assertEquals(0L, instance.lastChange);
		instance.version = 1L;
		instance.lastChange = System.currentTimeMillis();
		instance.name = "test " + instance;
		instance = dao.update(instance);
		assertEquals(1L, instance.version);
		assertTrue(instance.lastChange > 0);
		dao.delete(instance);
	}

}
