package org.openlca.core.database;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.Actor;

public class ActorDaoTest {

	private ActorDao dao;

	@Before
	public void setUp() throws Exception {
		dao = new ActorDao(TestSession.getMySQLDatabase().getEntityFactory());
	}

	@Test
	public void testQuery() throws Exception {
		Actor actor = new Actor();
		actor.setId("--test--actor--id");
		actor.setName("--test--actor--name");
		actor.setAddress("--test--actor--addr");
		dao.insert(actor);
		String jpql = "select a from Actor a where a.name = :name";
		Actor alias = dao.getFirst(jpql,
				Collections.singletonMap("name", "--test--actor--name"));
		Assert.assertEquals(actor.getId(), alias.getId());
		dao.delete(alias);
	}
}
