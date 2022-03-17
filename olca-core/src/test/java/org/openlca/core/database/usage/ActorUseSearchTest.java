package org.openlca.core.database.usage;


import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;

public class ActorUseSearchTest {

	private final IDatabase db = Tests.getDb();

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
