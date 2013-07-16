package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.ListUtils;
import org.openlca.core.TestSession;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.lean.BaseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class CategorizedEntityDaoTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void runCrudTests() throws Exception {
		EntityManagerFactory emf = TestSession.getDefaultDatabase()
				.getEntityFactory();
		run(Actor.class, new ActorDao(emf));
		run(Source.class, new SourceDao(emf));
		run(UnitGroup.class, new UnitGroupDao(emf));
		run(FlowProperty.class, new FlowPropertyDao(emf));
		run(Flow.class, new FlowDao(emf));
		run(org.openlca.core.model.Process.class, new ProcessDao(emf));
		run(ImpactMethod.class, new ImpactMethodDao(emf));
		run(ProductSystem.class, new ProductSystemDao(emf));
		run(Project.class, new ProjectDao(emf));
	}

	private <T extends CategorizedEntity> void run(Class<T> clazz,
			CategorizedEnitityDao<T> dao) throws Exception {
		log.info("run category entity tests for {}", clazz);
		T instance = makeNew(clazz);
		dao.insert(instance);
		testFindForNullCategory(dao, instance);
		Category category = addCategory(clazz, dao, instance);
		testGetDescriptorsForCategory(dao, instance, category);
	}

	private <T extends CategorizedEntity> void testFindForNullCategory(
			CategorizedEnitityDao<T> dao, T instance) {
		Category cat = null;
		List<BaseDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		BaseDescriptor descriptor = ListUtils.findDescriptor(instance.getId(),
				descriptors);
		Assert.assertNotNull(descriptor);
	}

	private <T extends CategorizedEntity> Category addCategory(Class<T> clazz,
			CategorizedEnitityDao<T> dao, T instance) throws Exception {
		Category category = new Category();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.forModelClass(clazz));
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		instance.setCategory(category);
		dao.update(instance);
		return category;
	}

	private <T extends CategorizedEntity> void testGetDescriptorsForCategory(
			CategorizedEnitityDao<T> dao, T instance, Category category)
			throws Exception {
		List<BaseDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		BaseDescriptor descriptor = ListUtils.findDescriptor(instance.getId(),
				descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
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
