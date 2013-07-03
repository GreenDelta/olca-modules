package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Category;
import org.openlca.core.model.CostCategory;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessGroupSet;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductCostEntry;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @RunWith(Theories.class)
public class BaseDaoTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	// we cannot use @Theory and @DataPoints with an array of classes
	// see https://github.com/junit-team/junit/issues/76
	// @formatter:off
	// @DataPoints
	@SuppressWarnings("unchecked")
	private Class<? extends AbstractEntity>[] classes = new Class[] {
		Actor.class,
		AllocationFactor.class,
		Category.class,
		CostCategory.class,
		Exchange.class,
		Flow.class,
		FlowProperty.class,
		FlowPropertyFactor.class,
		ImpactCategory.class,
		ImpactFactor.class,
		ImpactMethod.class,
		Location.class,
		NormalizationWeightingFactor.class,
		NormalizationWeightingSet.class,
		Parameter.class,
		Process.class,
		ProcessGroupSet.class,
		ProcessDocumentation.class,
		ProcessLink.class,
		ProductCostEntry.class,
		ProductSystem.class,
		Project.class,
		Source.class,
		Unit.class,
		UnitGroup.class,
	};
	// @formatter:on

	@Test
	public void runTests() throws Exception {
		for (Class<? extends AbstractEntity> clazz : classes)
			testCrud(clazz);
	}

	// @Theory
	private <T extends AbstractEntity> void testCrud(Class<T> clazz)
			throws Exception {
		log.info("run base dao test with {}", clazz);
		T instance = clazz.newInstance();
		BaseDao<T> dao = new BaseDao<>(clazz, TestSession.getDefaultDatabase()
				.getEntityFactory());
		dao.insert(instance);
		dao.update(instance);
		TestSession.emptyCache();
		T alias = dao.getForId(instance.getId());
		Assert.assertEquals(alias, instance);
		dao.delete(instance);
		Assert.assertNull(dao.getForId(instance.getId()));
	}

}
