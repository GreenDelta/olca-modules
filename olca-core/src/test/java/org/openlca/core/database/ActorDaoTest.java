package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.ActorDescriptor;

import com.google.common.base.Optional;

public class ActorDaoTest {

	private ActorDao dao;

	@Before
	public void setUp() throws Exception {
		dao = new ActorDao(TestSession.getDefaultDatabase().getEntityFactory());
	}

	@Test
	public void testGetForEmptyCategory() throws Exception {
		Actor actor = createActor();
		dao.insert(actor);
		TestSession.emptyCache();
		Category cat = null;
		List<ActorDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		dao.delete(actor);
		Assert.assertTrue(descriptors.size() > 0);
		boolean found = false;
		for (ActorDescriptor d : descriptors)
			if (Objects.equals(d.getId(), actor.getId()))
				found = true;
		Assert.assertTrue(found);
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

	private Actor createActor() {
		Actor actor = new Actor();
		actor.setId(UUID.randomUUID().toString());
		actor.setName("name");
		actor.setAddress("addr");
		return actor;
	}
}
