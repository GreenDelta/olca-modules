package org.openlca.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.Descriptor;

public class EntityCacheTest {

	private final IDatabase db = Tests.getDb();
	private final EntityCache cache = EntityCache.create(db);

	@Test
	public void testNoEntity() {
		long NULL_KEY = 99999999999999999L;
		assertNull(cache.get(Actor.class, NULL_KEY));
		assertNull(cache.get(ActorDescriptor.class, NULL_KEY));
		var actors = cache.getAll(Actor.class,
				Arrays.asList(NULL_KEY, NULL_KEY + 1));
		assertEquals(0, actors.size());
		var descriptors = cache.getAll(
			ActorDescriptor.class, List.of(NULL_KEY, NULL_KEY + 1));
		assertEquals(0, descriptors.size());
		cache.invalidate(Actor.class, NULL_KEY);
		cache.invalidateAll(Actor.class, List.of(NULL_KEY, NULL_KEY + 1));
		cache.invalidateAll();
	}

	@Test
	public void testSingleEntity() {
		var actor = new Actor();
		actor.name = "test#actor";
		db.insert(actor);
		checkEntity(actor);
		db.delete(actor);
	}

	@Test
	public void testMultipleEntities() {
		var actors = new ArrayList<Actor>();
		for (int i = 0; i < 100; i++) {
			var actor = new Actor();
			actor.name = "test#actor " + i;
			db.insert(actor);
			actors.add(actor);
		}
		for (Actor actor : actors) {
			checkEntity(actor);
			db.delete(actor);
		}
	}

	@Test
	public void testRefresh() {
		var actor = new Actor();
		actor.name = "test#actor";
		db.insert(actor);
		checkEntity(actor);
		actor.name = "butterkuchen";
		db.update(actor);
		cache.refresh(Actor.class, actor.id);
		cache.refresh(ActorDescriptor.class, actor.id);
		checkEntity(actor);
		db.delete(actor);
	}

	@Test
	public void testInvalidate() {
		var actor = new Actor();
		actor.name = "test#actor";
		db.insert(actor);
		checkEntity(actor);
		actor.name = "butterkuchen";
		db.update(actor);
		cache.invalidate(Actor.class, actor.id);
		cache.invalidate(ActorDescriptor.class, actor.id);
		checkEntity(actor);
		db.delete(actor);
	}

	@Test
	public void testInvalidateAll() {
		Actor actor = new Actor();
		actor.name = "test#actor";
		db.insert(actor);
		checkEntity(actor);
		actor.name = "butterkuchen";
		db.update(actor);
		cache.invalidateAll();
		checkEntity(actor);
		db.delete(actor);
	}

	@Test
	public void testInvalidateAllForIds() {
		Actor actor = new Actor();
		actor.name = "test#actor";
		db.insert(actor);
		checkEntity(actor);
		actor.name = "butterkuchen";
		db.update(actor);
		cache.invalidateAll(Actor.class, List.of(actor.id));
		cache.invalidateAll(ActorDescriptor.class, List.of(actor.id));
		checkEntity(actor);
		db.delete(actor);
	}

	private void checkEntity(Actor actor) {
		var alias = cache.get(Actor.class, actor.id);
		assertEquals(actor, alias);
		assertEquals(actor.name, alias.name);
		var descriptor = cache.get(ActorDescriptor.class, actor.id);
		assertEquals(actor.name, descriptor.name);
		assertEquals(actor.id, descriptor.id);
		assertTrue(cache.getAll(Actor.class,
			List.of(actor.id)).containsValue(actor));
		assertTrue(cache.getAll(ActorDescriptor.class,
			List.of(actor.id)).containsValue(descriptor));
	}

	@Test
	public void testAllModels() throws Exception{
		for (var modelType : ModelType.values()) {
			var type = modelType.getModelClass();
			if (type == null)
				continue;

			// create it
			var m = type.getConstructor().newInstance();
			m.name = modelType.name();
			db.insert(m);
			assertEquals(m, cache.get(type, m.id));

			// descriptors
			var d = Descriptor.of(m);
			assertEquals(d, cache.get(d.getClass(), m.id));

			// delete it
			db.delete(m);
			cache.invalidate(type, m.id);
			cache.invalidate(d.getClass(), d.id);
			assertNull(cache.get(type, m.id));
			assertNull(cache.get(d.getClass(), d.id));
		}

	}

}
