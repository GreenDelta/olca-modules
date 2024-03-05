package org.openlca.git.actions;

import java.io.IOException;

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
	}

	private void firstCommit() throws IOException {
		repo.create("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty",
				"ACTOR/sub/empty");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty/.empty",
				"ACTOR/sub/1aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void secondCommit() throws IOException {
		repo.create("ACTOR/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.delete("ACTOR/sub/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void thirdCommit() throws IOException {
		repo.move("ACTOR/sub/2aa39f5b-5021-4b6b-9330-739f082dfae0.json", "sub/empty");
		repo.move("ACTOR/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json", "sub/empty");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/empty/.empty",
				"ACTOR/sub/empty/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
	}

	private void fourthCommit() throws IOException {
		repo.delete("ACTOR/empty");
		repo.move("ACTOR/sub/empty/2aa39f5b-5021-4b6b-9330-739f082dfae0.json", "");
		repo.move("ACTOR/sub/empty/3aa39f5b-5021-4b6b-9330-739f082dfae0.json", "sub");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/empty/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void fifthCommit() throws IOException {
		repo.move("ACTOR/sub/3aa39f5b-5021-4b6b-9330-739f082dfae0.json", "");
		repo.delete("ACTOR/sub/empty");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/3aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/sub/.empty",
				RepositoryInfo.FILE_NAME);
	}

	private void sixthCommit() throws IOException {
		repo.delete("ACTOR/sub",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/2aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/3aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		repo.assertEqualRecursive(repo.createIterator(),
				RepositoryInfo.FILE_NAME);
	}

}
