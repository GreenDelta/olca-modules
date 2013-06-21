package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.ListUtils;
import org.openlca.core.TestSession;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.ActorDescriptor;

import com.google.common.base.Optional;

public class ActorDaoTest {

	private ActorDao dao;
	private Actor actor;

	@Before
	public void setUp() throws Exception {
		dao = new ActorDao(TestSession.getDefaultDatabase().getEntityFactory());
		actor = createActor();
		dao.insert(actor);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(actor);
	}

	private Actor createActor() {
		Actor actor = new Actor();
		actor.setId(UUID.randomUUID().toString());
		actor.setName("name");
		actor.setAddress("addr");
		return actor;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<ActorDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		ActorDescriptor descriptor = ListUtils.findDescriptor(actor.getId(),
				descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<ActorDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		ActorDescriptor descriptor = ListUtils.findDescriptor(actor.getId(),
				descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.ACTOR);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		actor.setCategory(category);
		dao.update(actor);
		return category;
	}

	@Test
	public void testQuery() throws Exception {
		Actor actor = createActor();
		dao.insert(actor);
		TestSession.emptyCache();
		String jpql = "select a from Actor a where a.name = :name";
		List<Actor> results = dao.getAll(jpql,
				Collections.singletonMap("name", "name"));
		dao.delete(actor);
		Assert.assertTrue(results.size() > 0);
		boolean found = false;
		for (Actor a : results)
			if (Objects.equals(a.getId(), actor.getId()))
				found = true;
		Assert.assertTrue(found);
	}
}
