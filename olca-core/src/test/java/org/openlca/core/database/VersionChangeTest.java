package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;

public class VersionChangeTest {

	private Class<?>[] classes = { Actor.class, Source.class, UnitGroup.class,
			FlowProperty.class, Flow.class, Process.class, ProductSystem.class,
			ImpactMethod.class, Project.class };

	private IDatabase db = Tests.getDb();

	@Test
	public void testUpdate() throws Exception {
		for (Class<?> clazz : classes) {
			CategorizedEntity entity = (CategorizedEntity) clazz.newInstance();
			testUpdate(entity);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends CategorizedEntity> void testUpdate(T instance)
			throws Exception {
		BaseDao<T> dao = (BaseDao<T>) db.createDao(instance.getClass());
		instance = dao.insert(instance);
		Assert.assertEquals(0L, instance.getVersion());
		Assert.assertEquals(0L, instance.getLastChange());
		instance.setVersion(1L);
		instance.setLastChange(System.currentTimeMillis());
		instance.setName("test " + instance);
		instance = dao.update(instance);
		Assert.assertEquals(1L, instance.getVersion());
		Assert.assertTrue(instance.getLastChange() > 0);
		dao.delete(instance);
	}

}
