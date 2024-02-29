package org.openlca.git.repo;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;

public class DiffsTests {

	private static TmpConfig config;
	private static ClientRepository repo;
	private static Commit[] commits;

	@BeforeClass
	public static void createRepo() throws IOException {
		config = TmpConfig.create();
		repo = config.repo();
		commits = new Commit[] {
				repo.commits.get(RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_1)),
				repo.commits.get(RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_2)),
				repo.commits.get(RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_3))
		};
	}

	@Test
	public void testNoDiffWithDatabase() {
		Assert.assertTrue(repo.diffs.find().withDatabase().isEmpty());
	}

	@Test
	public void testDiffWithDatabase() {
		var diffs = repo.diffs.find().commit(commits[0]).withDatabase().stream().sorted().toList();
		Assert.assertEquals(9, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(5));
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(6));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(7));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(8));

		diffs = repo.diffs.find().commit(commits[1]).withDatabase().stream().sorted().toList();
		Assert.assertEquals(5, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
	}

	@Test
	public void testDiffWithDatabaseExcludeCategories() {
		var diffs = repo.diffs.find().commit(commits[0]).excludeCategories().withDatabase().stream().sorted().toList();
		Assert.assertEquals(8, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(5));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(6));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(7));
	}

	@Test
	public void testDiffWithDatabaseOnlyCategories() {
		var diffs = repo.diffs.find().commit(commits[0]).onlyCategories().withDatabase().stream().sorted().toList();
		Assert.assertEquals(1, diffs.size());
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(0));

		diffs = repo.diffs.find().commit(commits[1]).onlyCategories().withDatabase().stream().sorted().toList();
		Assert.assertEquals(0, diffs.size());
	}

	@Test
	public void testDiffWithCommit() {
		var diffs = repo.diffs.find().commit(commits[0]).with(commits[1]).stream().sorted().toList();
		Assert.assertEquals(4, diffs.size());
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(0));
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(1));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(2));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(3));

		diffs = repo.diffs.find().commit(commits[1]).with(commits[2]).stream().sorted().toList();
		Assert.assertEquals(5, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
	}

	@Test
	public void testDiffWithCommitExcludeCategories() {
		var diffs = repo.diffs.find().commit(commits[0]).excludeCategories().with(commits[1]).stream().sorted().toList();
		Assert.assertEquals(3, diffs.size());
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(1));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(2));
	}

	@Test
	public void testDiffWithCommitOnlyCategories() {
		var diffs = repo.diffs.find().commit(commits[0]).onlyCategories().with(commits[1]).stream().sorted().toList();
		Assert.assertEquals(1, diffs.size());
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(0));

		diffs = repo.diffs.find().commit(commits[1]).onlyCategories().with(commits[2]).stream().sorted().toList();
		Assert.assertEquals(0, diffs.size());
	}

	private void assertModel(DiffType expectedType, String expectedPath, Diff diff) {
		Assert.assertEquals(expectedType, diff.diffType);
		Assert.assertEquals(expectedPath, diff.path);
		Assert.assertFalse(diff.isCategory);
		Assert.assertFalse(diff.isEmptyCategory);
	}

	private void assertEmptyCategory(DiffType expectedType, String expectedPath, Diff diff) {
		Assert.assertEquals(expectedType, diff.diffType);
		Assert.assertEquals(expectedPath, diff.path);
		Assert.assertTrue(diff.isCategory);
		Assert.assertTrue(diff.isEmptyCategory);
	}

	@AfterClass
	public static void closeRepo() {
		config.close();
	}

}
