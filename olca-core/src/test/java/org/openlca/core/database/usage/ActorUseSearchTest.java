package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.usage.IUseSearch;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

public class ActorUseSearchTest {

	private IDatabase database = Tests.getDb();

	@Test
	public void testFindInProjects() {
		Actor actor = createActor(database);
		Project project = new Project();
		project.setName("project");
		project.setAuthor(actor);
		database.createDao(Project.class).insert(project);
		List<BaseDescriptor> results = IUseSearch.FACTORY.createFor(
				ModelType.ACTOR, database).findUses(
				Descriptors.toDescriptor(actor));
		database.createDao(Project.class).delete(project);
		database.createDao(Actor.class).delete(actor);
		BaseDescriptor expected = Descriptors.toDescriptor(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Actor createActor(IDatabase database) {
		Actor actor = new Actor();
		actor.setName("actor");
		database.createDao(Actor.class).insert(actor);
		return actor;
	}

	@Test
	public void testFindInProcesses() {
		Actor actor = createActor(database);
		Process process = createProcess(actor);
		List<BaseDescriptor> results = IUseSearch.FACTORY.createFor(
				ModelType.ACTOR, database).findUses(
				Descriptors.toDescriptor(actor));
		database.createDao(Process.class).delete(process);
		database.createDao(Actor.class).delete(actor);
		BaseDescriptor expected = Descriptors.toDescriptor(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess(Actor actor) {
		ProcessDocumentation documentation = new ProcessDocumentation();
		Process process = new Process();
		process.setName("process");
		process.setDocumentation(documentation);
		process.getDocumentation().setReviewer(actor);
		process.getDocumentation().setDataSetOwner(actor);
		process.getDocumentation().setDataGenerator(actor);
		process.getDocumentation().setDataDocumentor(actor);
		database.createDao(Process.class).insert(process);
		return process;
	}
}
