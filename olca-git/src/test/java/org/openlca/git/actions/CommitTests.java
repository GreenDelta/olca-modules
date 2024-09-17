package org.openlca.git.actions;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;

public class CommitTests extends AbstractRepositoryTests {

	@Test
	public void testCommit() throws IOException {
		firstCommit();
		secondCommit();
		thirdCommit();
		fourthCommit();
		fifthCommit();
		sixthCommit();
		// using empty categories, so version should be 3
		Assert.assertEquals(3, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(3, repo.getInfo().repositoryServerVersion());
	}

	@Test
	public void testCommitRepositoryInfoV2() throws IOException {
		repo.create("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/1aa39f5b4b6b9330.739f082DFae0.json");
		repo.commitWorkspace();
		// not using empty categories, so version should be 2
		Assert.assertEquals(2, repo.getInfo().repositoryClientVersion());
		Assert.assertEquals(2, repo.getInfo().repositoryServerVersion());
	}

	private void firstCommit() throws IOException {
		repo.create("ACTOR/0AA39f5b-5021-9330-739f082dfae0.json",
				"ACTOR/sub/1aa39f5b4b6b9330.739f082DFae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty",
				"ACTOR/sub/empty");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0AA39f5b-5021-9330-739f082dfae0.json",
				"ACTOR/empty/.empty",
				"ACTOR/sub/1aa39f5b4b6b9330.739f082DFae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void secondCommit() throws IOException {
		repo.create("ACTOR/empty/3.json");
		repo.delete("ACTOR/sub/1aa39f5b4b6b9330.739f082DFae0.json");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0AA39f5b-5021-9330-739f082dfae0.json",
				"ACTOR/empty/3.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void thirdCommit() throws IOException {
		repo.move("ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json", "sub/empty");
		repo.move("ACTOR/empty/3.json", "sub/empty");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0AA39f5b-5021-9330-739f082dfae0.json",
				"ACTOR/empty/.empty",
				"ACTOR/sub/empty/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/3.json",
				RepositoryInfo.FILE_NAME);
	}

	private void fourthCommit() throws IOException {
		repo.delete("ACTOR/empty");
		repo.move("ACTOR/sub/empty/2aa39f5b-5021-4b6b-9330-739f082dfae0.json", "");
		repo.move("ACTOR/sub/empty/3.json", "sub");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0AA39f5b-5021-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/3.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void fifthCommit() throws IOException {
		repo.move("ACTOR/sub/3.json", "");
		repo.delete("ACTOR/sub/empty");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0AA39f5b-5021-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/3.json",
				"ACTOR/sub/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void sixthCommit() throws IOException {
		repo.delete("ACTOR/sub",
				"ACTOR/0AA39f5b-5021-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/3.json");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				RepositoryInfo.FILE_NAME);
	}

}
