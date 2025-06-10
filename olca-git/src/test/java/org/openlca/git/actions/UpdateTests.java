package org.openlca.git.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.descriptors.Descriptors;
import org.openlca.core.model.Actor;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.repo.ClientRepository;
import org.openlca.util.Dirs;

public class UpdateTests extends AbstractRepositoryTests {

	private File otherDbDir;
	private IDatabase otherDb;
	private ClientRepository otherRepo;

	@Before
	public void before() throws IOException {
		otherDbDir = Files.createTempDirectory("olca-git-test").toFile();
		otherDb = new Derby(otherDbDir);
		otherRepo = new ClientRepository(repo.dir, otherDb, Descriptors.of(otherDb));
	}

	@Test
	public void testUpdate() throws IOException {
		repo.create("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		GitDatabaseUpdate.on(otherRepo).run();
		var actor = otherDb.get(Actor.class, "0aa39f5b-5021-4b6b-9330-739f082dfae0");
		Assert.assertNotNull(actor);
	}

	@After
	public void after() throws IOException {
		if (otherDbDir != null) {
			otherDb.close();
			Dirs.delete(otherDbDir);
		}
		if (otherRepo != null) {
			otherRepo.close();
		}
	}

}
