package org.openlca.core.database;

import java.util.List;
import java.util.Optional;
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
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CategorizedEntityDaoTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void runCrudTests() throws Exception {
		var db = Tests.getDb();
		run(Actor.class, new ActorDao(db));
		run(Source.class, new SourceDao(db));
		run(UnitGroup.class, new UnitGroupDao(db));
		run(FlowProperty.class, new FlowPropertyDao(db));
		run(Flow.class, new FlowDao(db));
		run(org.openlca.core.model.Process.class, new ProcessDao(db));
		run(ImpactMethod.class, new ImpactMethodDao(db));
		run(ImpactCategory.class, new ImpactCategoryDao(db));
		run(ProductSystem.class, new ProductSystemDao(db));
		run(Project.class, new ProjectDao(db));
		run(Currency.class, new CurrencyDao(db));
		run(SocialIndicator.class, new SocialIndicatorDao(db));
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void run(
			Class<T> clazz, CategorizedEntityDao<T, V> dao) throws Exception {
		log.trace("run category entity tests for {}", clazz);
		T instance = makeNew(clazz);
		dao.insert(instance);
		testFindForNullCategory(dao, instance);
		var category = addCategory(clazz, dao, instance);
		testGetDescriptorsForCategory(dao, instance, category);

		// test get for ref-ID
		T clone = dao.getForRefId(instance.refId);
		assertEquals(instance, clone);
		assertNull(dao.getForRefId(UUID.randomUUID().toString()));
	}


	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void testFindForNullCategory(
			CategorizedEntityDao<T, V> dao, T instance) {
		List<V> descriptors = dao.getDescriptors(Optional.empty());
		Descriptor descriptor = ListUtils.findDescriptor(instance.id,
				descriptors);
		Assert.assertNotNull(descriptor);
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> Category addCategory(
			Class<T> clazz, CategorizedEntityDao<T, V> dao, T instance) {
		Category category = new Category();
		category.refId = UUID.randomUUID().toString();
		category.name = "test_category";
		category.modelType = ModelType.forModelClass(clazz);
		CategoryDao catDao = new CategoryDao(Tests.getDb());
		catDao.insert(category);
		instance.category = category;
		dao.update(instance);
		return category;
	}

	private <T extends CategorizedEntity, V extends CategorizedDescriptor> void testGetDescriptorsForCategory(
			CategorizedEntityDao<T, V> dao, T instance, Category category) {
		List<V> descriptors = dao.getDescriptors(Optional.ofNullable(category));
		Descriptor descriptor = ListUtils.findDescriptor(instance.id,
				descriptors);
		Assert.assertNotNull(descriptor);
		new CategoryDao(Tests.getDb()).delete(category);
	}

	private <T extends CategorizedEntity> T makeNew(Class<T> clazz)
			throws Exception {
		T instance = clazz.getDeclaredConstructor().newInstance();
		instance.description = "description";
		instance.name = "name";
		instance.refId = UUID.randomUUID().toString();
		return instance;
	}

}
