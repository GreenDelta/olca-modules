package org.openlca.core.database;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;

public class ContainsTest {

	@Test
	public void test() throws Exception {
		IDatabase db = Tests.getDb();
		test(Actor.class, new ActorDao(db));
		test(Source.class, new SourceDao(db));
		test(UnitGroup.class, new UnitGroupDao(db));
		test(FlowProperty.class, new FlowPropertyDao(db));
		test(Flow.class, new FlowDao(db));
		test(org.openlca.core.model.Process.class, new ProcessDao(db));
		test(ProductSystem.class, new ProductSystemDao(db));
		test(Project.class, new ProjectDao(db));
		test(Location.class, new LocationDao(db));
	}

	private <T extends RootEntity> void test(Class<T> clazz,
			RootEntityDao<T, ?> dao) throws Exception {
		String refId = UUID.randomUUID().toString();
		Assert.assertFalse(dao.contains(refId));
		T entity = clazz.newInstance();
		entity.setRefId(refId);
		dao.insert(entity);
		Assert.assertTrue(dao.contains(refId));
		dao.delete(entity);
		Assert.assertFalse(dao.contains(refId));
	}

}
