package org.openlca.git.repo;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.git.Tests.TmpConfig;

public class CommitsTests {

	private static TmpConfig config;
	private static ClientRepository repo;
	private static String[] commitIds;

	@BeforeClass
	public static void createRepo() throws IOException {
		config = TmpConfig.create();
		repo = config.repo();
		commitIds = new String[] {
				RepoData.commit(repo, RepoData.EXAMPLE_COMMIT_1),
				RepoData.commit(repo, RepoData.EXAMPLE_COMMIT_2),
				RepoData.commit(repo, RepoData.EXAMPLE_COMMIT_3)
		};
	}

	@Test
	public void testAll() {
		var commits = Commits.of(repo).find().all();
		Assert.assertEquals(3, commits.size());
		for (var i = 0; i < commitIds.length; i++) {
			Assert.assertEquals(commitIds[i], commits.get(i).id);
		}
	}

	@Test
	public void testLatest() throws IOException {
		var commit = Commits.of(repo).find().latest();
		Assert.assertNotNull(commit);
		Assert.assertEquals(commitIds[commitIds.length - 1], commit.id);
	}

	@Test
	public void testLatestId() throws IOException {
		var commitId = Commits.of(repo).find().latestId();
		Assert.assertNotNull(commitId);
		Assert.assertEquals(commitIds[commitIds.length - 1], commitId);
	}

	@Test
	public void testBefore() throws IOException {
		var commits = Commits.of(repo).find().before(commitIds[2]).all();
		Assert.assertEquals(2, commits.size());
		Assert.assertEquals(commitIds[0], commits.get(0).id);
		Assert.assertEquals(commitIds[1], commits.get(1).id);
	}

	@Test
	public void testUntil() throws IOException {
		var commits = Commits.of(repo).find().until(commitIds[1]).all();
		Assert.assertEquals(2, commits.size());
		Assert.assertEquals(commitIds[0], commits.get(0).id);
		Assert.assertEquals(commitIds[1], commits.get(1).id);
	}

	@Test
	public void testFrom() throws IOException {
		var commits = Commits.of(repo).find().from(commitIds[1]).all();
		Assert.assertEquals(commitIds.length - 1, commits.size());
		for (var i = 1; i < commitIds.length; i++) {
			Assert.assertEquals(commitIds[i], commits.get(i - 1).id);
		}
	}

	@Test
	public void testAfter() throws IOException {
		var commits = Commits.of(repo).find().after(commitIds[1]).all();
		Assert.assertEquals(commitIds.length - 2, commits.size());
		for (var i = 2; i < commitIds.length; i++) {
			Assert.assertEquals(commitIds[i], commits.get(i - 2).id);
		}
	}

	@Test
	public void testModelType() throws IOException {
		var commits = Commits.of(repo).find().type(ModelType.ACTOR).all();
		Assert.assertEquals(2, commits.size());
		Assert.assertEquals(commitIds[0], commits.get(0).id);
		Assert.assertEquals(commitIds[2], commits.get(1).id);
	}

	@Test
	public void testModel() throws IOException {
		var commits = Commits.of(repo).find()
				.model(ModelType.SOURCE, "aca49f5b-5021-4b6b-9330-739f082dfae0").all();
		Assert.assertEquals(2, commits.size());
		Assert.assertEquals(commitIds[0], commits.get(0).id);
		Assert.assertEquals(commitIds[1], commits.get(1).id);

		commits = Commits.of(repo).find()
				.model(ModelType.ACTOR, "0aa39f5b-5021-4b6b-9330-739f082dfae0").all();
		Assert.assertEquals(2, commits.size());
		Assert.assertEquals(commitIds[0], commits.get(0).id);
		Assert.assertEquals(commitIds[2], commits.get(1).id);
	}

	@Test
	public void testPath() throws IOException {
		var commits = Commits.of(repo).find().path("FLOW/cat").all();
		Assert.assertEquals(1, commits.size());
		Assert.assertEquals(commitIds[0], commits.get(0).id);

		commits = Commits.of(repo).find().path("ACTOR").all();
		Assert.assertEquals(2, commits.size());
		Assert.assertEquals(commitIds[0], commits.get(0).id);
		Assert.assertEquals(commitIds[2], commits.get(1).id);
	}

	@AfterClass
	public static void closeRepo() {
		config.close();
	}

}
