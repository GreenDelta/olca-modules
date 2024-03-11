package org.openlca.git.actions;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Instant;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.actions.ConflictResolver.ConflictResolution;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.model.ModelRef;
import org.openlca.git.util.ModelRefMap;
import org.openlca.util.Dirs;

import com.google.gson.JsonObject;

public class MergeTests extends AbstractRepositoryTests {

	private File remoteDir;
	private TestRepository otherRepo;

	@Override
	public void createRepo() throws IOException, GitAPIException, URISyntaxException {
		remoteDir = Files.createTempDirectory("olca-git-test").toFile();
		GitInit.in(remoteDir).run();
		super.createRepo();
		otherRepo = new TestRepository(getRemotePath());
	}

	@Override
	protected String getRemotePath() {
		return remoteDir.getAbsolutePath();
	}

	@Test
	public void testMerge() throws IOException, GitAPIException {
		repo.create("ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		GitPush.from(repo).run();
		GitFetch.to(otherRepo).run();
		var result = GitMerge.on(otherRepo).run();
		Assert.assertEquals(MergeResult.SUCCESS, result);
		otherRepo.assertEqualRecursive(otherRepo.createIterator(),
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
		otherRepo.assertDatabaseEquals("ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	@Test
	public void testPushRejected() throws IOException, GitAPIException {
		repo.create("ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		GitPush.from(repo).run();
		GitFetch.to(otherRepo).run();
		GitMerge.on(otherRepo).run();
		otherRepo.assertDatabaseEquals(
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");

		repo.modify("ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		GitPush.from(repo).run();

		otherRepo.modify("ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.commitWorkspace();
		var response = GitPush.from(otherRepo).run();
		Assert.assertEquals(Status.REJECTED_NONFASTFORWARD, response.status());
	}

	@Test
	public void testMergeInfoOlderVersion() throws IOException, GitAPIException {
		repo.create("ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		Assert.assertEquals(2, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(2, repo.getInfo().repositoryServerVersion());

		otherRepo.create("ACTOR/empty");
		otherRepo.commitWorkspace();
		Assert.assertEquals(3, otherRepo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, otherRepo.getInfo().repositoryServerVersion());

		GitPush.from(repo).run();
		GitFetch.to(otherRepo).run();
		GitMerge.on(otherRepo).run();
		Assert.assertEquals(3, otherRepo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, otherRepo.getInfo().repositoryServerVersion());

		GitPush.from(otherRepo).run();
		GitFetch.to(repo).run();
		GitMerge.on(repo).run();
		Assert.assertEquals(3, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, repo.getInfo().repositoryServerVersion());
	}

	@Test
	public void testMergeInfoNewerVersions() throws IOException, GitAPIException {
		repo.create("ACTOR/empty");
		repo.commitWorkspace();
		Assert.assertEquals(3, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, repo.getInfo().repositoryServerVersion());

		otherRepo.create("ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.commitWorkspace();
		Assert.assertEquals(2, otherRepo.getInfo().repositoryClientVersion());
		Assert.assertEquals(2, otherRepo.getInfo().repositoryServerVersion());

		GitPush.from(repo).run();
		GitFetch.to(otherRepo).run();
		GitMerge.on(otherRepo).run();
		Assert.assertEquals(3, otherRepo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, otherRepo.getInfo().repositoryServerVersion());

		GitPush.from(otherRepo).run();
		GitFetch.to(repo).run();
		GitMerge.on(repo).run();
		Assert.assertEquals(3, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, repo.getInfo().repositoryServerVersion());
	}

	@Test
	public void testMergeInfoResultsInEmptyCategory() throws IOException, GitAPIException {
		// first commit on repo 1 and push
		repo.create("ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		GitPush.from(repo).run();
		Assert.assertEquals(2, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(2, repo.getInfo().repositoryServerVersion());

		// fetch from repo 1 and merge into repo 2
		GitFetch.to(otherRepo).run();
		GitMerge.on(otherRepo).run();
		Assert.assertEquals(2, otherRepo.getInfo().repositoryClientVersion());
		Assert.assertEquals(2, otherRepo.getInfo().repositoryServerVersion());
		otherRepo.assertDatabaseEquals(
				"ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);

		// move datasets in repo 1, commit and push
		repo.move("ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", "category2");
		repo.move("ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json", "category");
		repo.commitWorkspace();
		GitPush.from(repo)
				.run();
		repo.assertDatabaseEquals(
				"ACTOR/category/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/0aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/category/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
		Assert.assertEquals(2, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(2, repo.getInfo().repositoryServerVersion());

		// modify and commit datasets in repo 2 to create conflict
		otherRepo.modify("ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.commitWorkspace();
		otherRepo.assertDatabaseEquals(
				"ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.assertEqualRecursive(otherRepo.createIterator(),
				"ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
		Assert.assertEquals(2, otherRepo.getInfo().repositoryClientVersion());
		Assert.assertEquals(2, otherRepo.getInfo().repositoryServerVersion());

		// pull and resolve conflicts
		GitFetch.to(otherRepo).run();
		GitMerge.on(otherRepo)
				.resolveConflictsWith(new StaticConflictResolutions(new ModelRefMap<ConflictResolution>()
						.put(new ModelRef("ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json"),
								ConflictResolution.keep())
						.put(new ModelRef("ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json"),
								ConflictResolution.overwrite())))
				.run();
		otherRepo.assertDatabaseEquals(
				"ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2");
		otherRepo.assertEqualRecursive(otherRepo.createIterator(),
				"ACTOR/category/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category2/.empty",
				RepositoryInfo.FILE_NAME);
		Assert.assertEquals(3, otherRepo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, otherRepo.getInfo().repositoryServerVersion());
	}

	@Test(expected = ConflictException.class)
	public void testUnresolvedConflict() throws IOException, GitAPIException {
		runConflictTest(null, 0);
	}

	@Test
	public void testConflictResolutionIsEqual() throws IOException, GitAPIException {
		runConflictTest(ConflictResolution.isEqual(), 0);
	}

	@Test
	public void testConflictResolutionKeep() throws IOException, GitAPIException {
		runConflictTest(ConflictResolution.keep(), 0);
		otherRepo.assertDatabaseEquals(
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.assertEqualRecursive(otherRepo.createIterator(),
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
	}

	@Test
	public void testConflictResolutionOverwrite() throws IOException, GitAPIException {
		runConflictTest(ConflictResolution.overwrite(), 1);
		otherRepo.assertDatabaseEquals(
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.assertEqualRecursive(otherRepo.createIterator(),
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
	}

	@Test
	public void testConflictResolutionMerge() throws IOException, GitAPIException {
		var merged = new JsonObject();
		merged.addProperty("@type", "Actor");
		merged.addProperty("@id", "1aa39f5b-5021-4b6b-9330-739f082dfae0");
		merged.addProperty("version", Version.asString(Version.valueOf(1, 1, 1)));
		merged.addProperty("lastChange", Instant.ofEpochMilli(System.currentTimeMillis()).toString());
		runConflictTest(ConflictResolution.merge(merged), 1);
		otherRepo.assertDatabaseEquals(
				"ACTOR/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.assertEqualRecursive(otherRepo.createIterator(),
				"ACTOR/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
	}

	private void runConflictTest(ConflictResolution resolution, int expectedMergedChanges)
			throws IOException, GitAPIException {
		repo.create("ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		GitPush.from(repo).run();
		GitFetch.to(otherRepo).run();
		GitMerge.on(otherRepo).run();
		otherRepo.assertDatabaseEquals(
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		otherRepo.assertEqualRecursive(otherRepo.createIterator(),
				"ACTOR/test/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);

		repo.modify("ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		GitPush.from(repo).run();
		if (resolution == null || resolution.type != ConflictResolutionType.IS_EQUAL) {
			otherRepo.modify("ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		} else {
			var actor = repo.database.get(Actor.class, "1aa39f5b-5021-4b6b-9330-739f082dfae0");
			var otherActor = otherRepo.database.get(Actor.class, "1aa39f5b-5021-4b6b-9330-739f082dfae0");
			otherActor.version = actor.version;
			otherActor.lastChange = actor.lastChange;
			otherRepo.database.update(otherActor);
		}
		otherRepo.commitWorkspace();
		GitFetch.to(otherRepo).run();
		GitMerge.on(otherRepo)
				.resolveConflictsWith(new StaticConflictResolutions(new ModelRefMap<ConflictResolution>().put(
						new ModelRef("ACTOR/test/1aa39f5b-5021-4b6b-9330-739f082dfae0.json"),
						resolution)))
				.as(committer)
				.run();
		var commits = otherRepo.commits.find().all();
		Assert.assertEquals(4, commits.size());
		var mergeCommit = commits.get(commits.size() - 1);
		var merged = otherRepo.diffs.find().commit(mergeCommit).withPreviousCommit();
		Assert.assertEquals(expectedMergedChanges, merged.size());
	}

	@Override
	public void closeRepo() throws IOException {
		super.closeRepo();
		otherRepo.close();
		Dirs.delete(remoteDir);
	}

	private class StaticConflictResolutions implements ConflictResolver {

		private final ModelRefMap<ConflictResolution> resolutions;

		private StaticConflictResolutions(ModelRefMap<ConflictResolution> resolutions) {
			this.resolutions = resolutions;
		}

		@Override
		public boolean isConflict(ModelRef ref) {
			return resolutions.contains(ref);
		}

		@Override
		public ConflictResolution resolveConflict(ModelRef ref, JsonObject fromHistory) {
			return resolutions.get(ref);
		}

		@Override
		public ConflictResolutionType peekConflictResolution(ModelRef ref) {
			var resolution = resolutions.get(ref);
			if (resolution == null)
				return null;
			return resolution.type;
		}

	}

}
