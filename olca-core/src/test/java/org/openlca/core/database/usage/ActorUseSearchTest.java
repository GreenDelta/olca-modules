package org.openlca.core.database.usage;


import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;

public class ActorUseSearchTest {

	private final IDatabase db = Tests.getDb();

	// TODO: #model-doc search for reviewers

	@Test
	public void testFindNoUsage() {
		var actor = db.insert(Actor.of("actor"));
		UsageTests.expectEmpty(actor);
		db.delete(actor);
	}

	@Test
	public void testFindInProcesses() {
		var actor = db.insert(Actor.of("actor"));
		var process = createProcess(actor);
		UsageTests.expectOne(actor, process);
		db.delete(process, actor);
	}

	private Process createProcess(Actor actor) {
		var documentation = new ProcessDoc();
		var process = new Process();
		process.name = "process";
		process.documentation = documentation;
		process.documentation.dataOwner = actor;
		process.documentation.dataGenerator = actor;
		process.documentation.dataDocumentor = actor;
		return db.insert(process);
	}
}
