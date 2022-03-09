package org.openlca.core.database.usage;


import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.descriptors.Descriptor;

public class ActorUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private final IUseSearch search = IUseSearch.of(ModelType.ACTOR, db);

	@Test
	public void testFindNoUsage() {
		var actor = db.insert(Actor.of("actor"));
		var models = search.find(actor.id);
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		db.delete(actor);
	}

	@Test
	public void testFindInProcesses() {
		var actor = db.insert(Actor.of("actor"));
		var process = createProcess(actor);
		var dep = search.find(actor.id);
		db.delete(process, actor);
		var expected = Descriptor.of(process);
		Assert.assertEquals(1, dep.size());
		Assert.assertEquals(expected, dep.iterator().next());
	}

	private Process createProcess(Actor actor) {
		var documentation = new ProcessDocumentation();
		var process = new Process();
		process.name = "process";
		process.documentation = documentation;
		process.documentation.reviewer = actor;
		process.documentation.dataSetOwner = actor;
		process.documentation.dataGenerator = actor;
		process.documentation.dataDocumentor = actor;
		return db.insert(process);
	}
}
