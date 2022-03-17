package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.usage.UsageSearch;
import org.openlca.core.model.*;
import org.openlca.core.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @RunWith(Theories.class)
public class BaseDaoTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// we cannot use @Theory and @DataPoints with an array of classes
	// see https://github.com/junit-team/junit/issues/76
	// @DataPoints
	@SuppressWarnings("unchecked")
	private final Class<? extends AbstractEntity>[] classes = new Class[]{
		Actor.class,
		AllocationFactor.class,
		Category.class,
		Currency.class,
		Epd.class,
		Exchange.class,
		Flow.class,
		FlowProperty.class,
		FlowPropertyFactor.class,
		ImpactCategory.class,
		ImpactFactor.class,
		ImpactMethod.class,
		Location.class,
		NwFactor.class,
		NwSet.class,
		Parameter.class,
		Process.class,
		ProcessGroupSet.class,
		ProcessDocumentation.class,
		ProductSystem.class,
		Project.class,
		SocialIndicator.class,
		Source.class,
		Unit.class,
		UnitGroup.class,
		Result.class,
	};

	@Test
	public void runTests() throws Exception {
		for (Class<? extends AbstractEntity> clazz : classes) {
			try {
				testCrud(clazz);
			} catch (Exception e) {
				throw new Exception("CRUD functions failed for " + clazz, e);
			}
		}
	}

	// @Theory
	private <T extends AbstractEntity> void testCrud(Class<T> clazz)
		throws Exception {
		log.trace("run base dao test with {}", clazz);
		T instance = clazz.getConstructor().newInstance();
		BaseDao<T> dao = Daos.base(Tests.getDb(), clazz);
		dao.insert(instance);
		testUsage(instance);
		dao.update(instance);
		Tests.emptyCache();
		T alias = dao.getForId(instance.id);
		Assert.assertEquals(alias, instance);
		dao.delete(instance);
		Assert.assertNull(dao.getForId(instance.id));
	}

	private <T extends AbstractEntity> void testUsage(T instance) {
		log.trace("test simple usage tests with {}", instance);
		var clazz = instance.getClass();
		if (!RootEntity.class.isAssignableFrom(clazz))
			return;
		var entity = (RootEntity) instance;
		var type = ModelType.forModelClass(clazz);
		var dependents = UsageSearch.of(type, Tests.getDb())
			.find(entity.id);
		Assert.assertTrue(dependents.isEmpty());
	}
}
