package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

public class ActorUseSearchTest {

	@Test
	public void testFindInProjects() {

		IDatabase database = TestSession.getDefaultDatabase();
		int occurences = 0;

		Actor actor = new Actor();
		actor.setName("actor");
		database.createDao(Actor.class).insert(actor);

		Project project = new Project();
		project.setName("project");
		project.setAuthor(actor);
		database.createDao(Project.class).insert(project);

		ProcessDocumentation documentation = new ProcessDocumentation();

		Process process = new Process();
		process.setName("process");
		process.setDocumentation(documentation);
		process.getDocumentation().setReviewer(actor);
		database.createDao(Process.class).insert(process);

		List<BaseDescriptor> results = new ActorUseSearch(database)
				.findUses(Descriptors.toDescriptor(actor));

		database.createDao(Project.class).delete(project);
		database.createDao(Process.class).delete(process);
		database.createDao(Actor.class).delete(actor);

		for (BaseDescriptor result : results) {
			if (result.getModelType() == ModelType.PROJECT) {
				occurences++;
			}
		}

		Assert.assertEquals(occurences, 1);
	}

	@Test
	public void testFindInProcesses() {

		IDatabase database = TestSession.getDefaultDatabase();
		int occurences = 0;

		Actor actor = new Actor();
		actor.setName("actor");
		database.createDao(Actor.class).insert(actor);

		ProcessDocumentation documentation = new ProcessDocumentation();

		Process process = new Process();
		process.setName("process");
		process.setDocumentation(documentation);
		process.getDocumentation().setReviewer(actor);
		process.getDocumentation().setDataSetOwner(actor);
		process.getDocumentation().setDataGenerator(actor);
		process.getDocumentation().setDataDocumentor(actor);
		database.createDao(Process.class).insert(process);

		List<BaseDescriptor> results = new ActorUseSearch(database)
				.findUses(Descriptors.toDescriptor(actor));

		database.createDao(Process.class).delete(process);
		database.createDao(Actor.class).delete(actor);

		for (BaseDescriptor result : results) {
			if (result.getModelType() == ModelType.PROCESS) {
				occurences++;
			}
		}

		Assert.assertEquals(occurences, 1);
	}
}
