package org.openlca.core.database;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.usage.IUseSearch;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessGroupSet;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @RunWith(Theories.class)
public class BaseDaoTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	// we cannot use @Theory and @DataPoints with an array of classes
	// see https://github.com/junit-team/junit/issues/76
	// @DataPoints
	@SuppressWarnings("unchecked")
	private Class<? extends AbstractEntity>[] classes = new Class[] {
			Actor.class,
			AllocationFactor.class,
			Category.class,
			Currency.class,
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
		Class<?> clazz = instance.getClass();
		if (!CategorizedEntity.class.isAssignableFrom(clazz))
			return;
		CategorizedEntity entity = (CategorizedEntity) instance;
		ModelType type = ModelType.forModelClass(clazz);
		List<CategorizedDescriptor> descriptors = IUseSearch.FACTORY
				.createFor(type, Tests.getDb())
				.findUses(Descriptor.of(entity));
		Assert.assertTrue(descriptors.isEmpty());
	}
}
