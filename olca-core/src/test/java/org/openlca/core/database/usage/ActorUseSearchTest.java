package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.ActorDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

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
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(actor));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		new ActorDao(database).delete(actor);
	}

	@Test
	public void testFindInProjects() {
		Actor actor = createActor();
		Project project = new Project();
		project.name = "project";
		project.author = actor;
		new ProjectDao(database).insert(project);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(actor));
		new ProjectDao(database).delete(project);
		new ActorDao(database).delete(actor);
		Descriptor expected = Descriptor.of(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Actor createActor() {
		Actor actor = new Actor();
		actor.name = "actor";
		return new ActorDao(database).insert(actor);
	}

	@Test
	public void testFindInProcesses() {
		Actor actor = createActor();
		Process process = createProcess(actor);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(actor));
		new ProcessDao(database).delete(process);
		new ActorDao(database).delete(actor);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess(Actor actor) {
		ProcessDocumentation documentation = new ProcessDocumentation();
		Process process = new Process();
		process.name = "process";
		process.documentation = documentation;
		process.documentation.reviewer = actor;
		process.documentation.dataSetOwner = actor;
		process.documentation.dataGenerator = actor;
		process.documentation.dataDocumentor = actor;
		return new ProcessDao(database).insert(process);
	}

	@Test
	public void testFindInMethods() {
		Actor author = createActor();
		ImpactMethod method = new ImpactMethod();
		method.name = "method";
		method.author = author;
		method = new ImpactMethodDao(database).insert(method);
		List<CategorizedDescriptor> results = search.findUses(Descriptor.of(author));
		Descriptor expected = Descriptor.of(method);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
		new ImpactMethodDao(database).delete(method);
		new ActorDao(database).delete(author);

		Actor generator = createActor();
		method = new ImpactMethod();
		method.name = "method";
		method.generator = generator;
		method = new ImpactMethodDao(database).insert(method);
		results = search.findUses(Descriptor.of(generator));
		expected = Descriptor.of(method);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
		new ImpactMethodDao(database).delete(method);
		new ActorDao(database).delete(generator);
	}

}
