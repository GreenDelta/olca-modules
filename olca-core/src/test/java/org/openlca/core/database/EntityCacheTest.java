package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.descriptors.ActorDescriptor;

public class EntityCacheTest {

	private EntityCache cache;
	private IDatabase database;

	@Before
	public void setUp() {
		database = Tests.getDb();
		cache = EntityCache.create(database);
	}

	@Test
	public void testNoEntity() throws Exception {
		Assert.assertNull(cache.get(Actor.class, 99999999999999999L));
		Assert.assertNull(cache.get(ActorDescriptor.class, 99999999999999999L));
		Map<Long, Actor> actors = cache.getAll(Actor.class,
				Arrays.asList(99999999999L, 88888888888L));
		Assert.assertEquals(0, actors.size());
		Map<Long, ActorDescriptor> descriptors = cache.getAll(
				ActorDescriptor.class,
				Arrays.asList(99999999999L, 88888888888L));
		Assert.assertEquals(0, descriptors.size());
		cache.invalidate(Actor.class, 99999999999999999L);
		cache.invalidateAll(Actor.class,
				Arrays.asList(99999999999L, 88888888888L));
		cache.invalidateAll();
	}

	@Test
	public void testSingleEntity() throws Exception {
		Actor actor = new Actor();
		actor.setName("test#actor");
		database.createDao(Actor.class).insert(actor);
		checkEntity(actor);
		database.createDao(Actor.class).delete(actor);
	}

	@Test
	public void testMultipleEntites() throws Exception {
		List<Actor> actors = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Actor actor = new Actor();
			actor.setName("test#actor " + i);
			database.createDao(Actor.class).insert(actor);
			actors.add(actor);
		}
		for (Actor actor : actors) {
			checkEntity(actor);
			database.createDao(Actor.class).delete(actor);
		}
	}

	@Test
	public void testRefresh() throws Exception {
		ActorDao dao = new ActorDao(database);
		Actor actor = new Actor();
		actor.setName("test#actor");
		dao.insert(actor);
		checkEntity(actor);
		actor.setName("butterkuchen");
		dao.update(actor);
		cache.refresh(Actor.class, actor.getId());
		cache.refresh(ActorDescriptor.class, actor.getId());
		checkEntity(actor);
	}

	@Test
	public void testInvalidate() throws Exception {
		ActorDao dao = new ActorDao(database);
		Actor actor = new Actor();
		actor.setName("test#actor");
		dao.insert(actor);
		checkEntity(actor);
		actor.setName("butterkuchen");
		dao.update(actor);
		cache.invalidate(Actor.class, actor.getId());
		cache.invalidate(ActorDescriptor.class, actor.getId());
		checkEntity(actor);
	}

	@Test
	public void testInvalidateAll() throws Exception {
		ActorDao dao = new ActorDao(database);
		Actor actor = new Actor();
		actor.setName("test#actor");
		dao.insert(actor);
		checkEntity(actor);
		actor.setName("butterkuchen");
		dao.update(actor);
		cache.invalidateAll();
		checkEntity(actor);
	}

	@Test
	public void testInvalidateAllForIds() throws Exception {
		ActorDao dao = new ActorDao(database);
		Actor actor = new Actor();
		actor.setName("test#actor");
		dao.insert(actor);
		checkEntity(actor);
		actor.setName("butterkuchen");
		dao.update(actor);
		cache.invalidateAll(Actor.class, Arrays.asList(actor.getId()));
		cache.invalidateAll(ActorDescriptor.class, Arrays.asList(actor.getId()));
		checkEntity(actor);
	}

	private void checkEntity(Actor actor) throws Exception {
		Actor alias = cache.get(Actor.class, actor.getId());
		Assert.assertEquals(actor, alias);
		Assert.assertEquals(actor.getName(), alias.getName());
		ActorDescriptor descriptor = cache.get(ActorDescriptor.class,
				actor.getId());
		Assert.assertEquals(actor.getName(), descriptor.getName());
		Assert.assertEquals(actor.getId(), descriptor.getId());
		Assert.assertTrue(cache.getAll(Actor.class,
				Arrays.asList(actor.getId())).containsValue(actor));
		Assert.assertTrue(cache.getAll(ActorDescriptor.class,
				Arrays.asList(actor.getId())).containsValue(descriptor));
	}

}
