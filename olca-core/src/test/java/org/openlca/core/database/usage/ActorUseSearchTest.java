package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

public class ActorUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<ActorDescriptor> search;

	@Before
	public void setup() {
		search = IUseSearch.FACTORY.createFor(ModelType.ACTOR, database);
	}

	@Test
	public void testFindNoUsage() {
		Actor actor = createActor();
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(actor));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		database.createDao(Actor.class).delete(actor);
	}

	@Test
	public void testFindInProjects() {
		Actor actor = createActor();
		Project project = new Project();
		project.setName("project");
		project.setAuthor(actor);
		database.createDao(Project.class).insert(project);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(actor));
		database.createDao(Project.class).delete(project);
		database.createDao(Actor.class).delete(actor);
		BaseDescriptor expected = Descriptors.toDescriptor(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Actor createActor() {
		Actor actor = new Actor();
		actor.setName("actor");
		database.createDao(Actor.class).insert(actor);
		return actor;
	}

	@Test
	public void testFindInProcesses() {
		Actor actor = createActor();
		Process process = createProcess(actor);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(actor));
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
