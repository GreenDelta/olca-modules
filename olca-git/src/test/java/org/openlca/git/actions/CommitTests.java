package org.openlca.git.actions;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Change;

public class CommitTests extends AbstractRepositoryTests {

	@Test
	public void testCommit() throws IOException {
		firstCommit();
		secondCommit();
		thirdCommit();
		fourthCommit();
		fifthCommit();
		sixthCommit();
	}

	private void firstCommit() throws IOException {
		create("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty",
				"ACTOR/sub/empty");
		commitChanges();
		assertEqualRecursive(createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty/.empty",
				"ACTOR/sub/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void secondCommit() throws IOException {
		create("ACTOR/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		delete("ACTOR/sub/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		commitChanges();
		assertEqualRecursive(createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void thirdCommit() throws IOException {
		move("ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json", "sub/empty");
		move("ACTOR/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json", "sub/empty");
		commitChanges();
		assertEqualRecursive(createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty/.empty",
				"ACTOR/sub/empty/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
	}

	private void fourthCommit() throws IOException {
		delete("ACTOR/empty");
		move("ACTOR/sub/empty/2aa39f5b-5021-4b6b-9330-739f082dfae0.json", "");
		move("ACTOR/sub/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json", "sub");
		commitChanges();
		assertEqualRecursive(createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void fifthCommit() throws IOException {
		move("ACTOR/sub/3aa39f5b-5021-4b6b-9330-739f082dfae0.json", "");
		delete("ACTOR/sub/empty");
		commitChanges();
		assertEqualRecursive(createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void sixthCommit() throws IOException {
		delete("ACTOR/sub",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/3aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		commitChanges();
		assertEqualRecursive(createIterator(),
				RepositoryInfo.FILE_NAME);
	}

	private void commitChanges() throws IOException {
		var diffs = repo.diffs.find().withDatabase();
		GitCommit.on(repo)
				.as(committer)
				.changes(Change.of(diffs))
				.withMessage("commit")
				.run();
		Assert.assertEquals(0, repo.diffs.find().withDatabase().size());
	}

}
