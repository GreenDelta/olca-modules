package org.openlca.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.ListUtils;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class CategorizedEntityDaoTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void runCrudTests() throws Exception {
		IDatabase database = Tests.getDb();
		run(Actor.class, new ActorDao(database));
		run(Source.class, new SourceDao(database));
		run(UnitGroup.class, new UnitGroupDao(database));
		run(FlowProperty.class, new FlowPropertyDao(database));
		run(Flow.class, new FlowDao(database));
		run(org.openlca.core.model.Process.class, new ProcessDao(database));
		run(ImpactMethod.class, new ImpactMethodDao(database));
		run(ProductSystem.class, new ProductSystemDao(database));
		run(Project.class, new ProjectDao(database));
		run(Currency.class, new CurrencyDao(database));
		run(SocialIndicator.class, new SocialIndicatorDao(database));
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void run(
			Class<T> clazz, CategorizedEntityDao<T, V> dao) throws Exception {
		log.trace("run category entity tests for {}", clazz);
		T instance = makeNew(clazz);
		dao.insert(instance);
		testFindForNullCategory(dao, instance);
		Category category = addCategory(clazz, dao, instance);
		testGetDescriptorsForCategory(dao, instance, category);
		testGetForRefId(dao, instance);
	}

	private <T extends CategorizedEntity> void testGetForRefId(
			CategorizedEntityDao<T, ?> dao, T instance) {
		T clone = dao.getForRefId(instance.getRefId());
		assertEquals(instance, clone);
		assertNull(dao.getForRefId(UUID.randomUUID().toString()));
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void testFindForNullCategory(
			CategorizedEntityDao<T, V> dao, T instance) {
		Category cat = null;
		List<V> descriptors = dao.getDescriptors(Optional.fromNullable(cat));
		BaseDescriptor descriptor = ListUtils.findDescriptor(instance.getId(),
				descriptors);
		Assert.assertNotNull(descriptor);
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> Category addCategory(
			Class<T> clazz, CategorizedEntityDao<T, V> dao, T instance) {
		Category category = new Category();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.forModelClass(clazz));
		BaseDao<Category> catDao = Tests.getDb().createDao(
				Category.class);
		catDao.insert(category);
		instance.setCategory(category);
		dao.update(instance);
		return category;
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void testGetDescriptorsForCategory(
			CategorizedEntityDao<T, V> dao, T instance, Category category) {
		List<V> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		BaseDescriptor descriptor = ListUtils.findDescriptor(instance.getId(),
				descriptors);
		Assert.assertNotNull(descriptor);
		Tests.getDb().createDao(Category.class)
				.delete(category);
	}

	private <T extends CategorizedEntity> T makeNew(Class<T> clazz)
			throws Exception {
		T instance = clazz.newInstance();
		instance.setDescription("descriptiom");
		instance.setName("name");
		instance.setRefId(UUID.randomUUID().toString());
		return instance;
	}

}
