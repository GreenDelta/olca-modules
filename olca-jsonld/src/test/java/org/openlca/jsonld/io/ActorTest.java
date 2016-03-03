package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ActorTest extends AbstractZipTest {

	@Test
	public void testActor() throws Exception {
		ActorDao dao = new ActorDao(Tests.getDb());
		Actor actor = createModel(dao);
		doExport(actor, dao);
		doImport(dao, actor);
		dao.delete(actor);
	}

	private Actor createModel(ActorDao dao) {
		Actor actor = new Actor();
		actor.setName("actor");
		actor.setRefId(UUID.randomUUID().toString());
		dao.insert(actor);
		return actor;
	}

	private void doExport(Actor actor, ActorDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(actor);
		});
		dao.delete(actor);
		Assert.assertFalse(dao.contains(actor.getRefId()));
	}

	private void doImport(ActorDao dao, Actor actor) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(actor.getRefId()));
		Actor clone = dao.getForRefId(actor.getRefId());
		Assert.assertEquals(actor.getName(), clone.getName());
	}
}
