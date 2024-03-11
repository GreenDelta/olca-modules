package org.openlca.git.repo;

import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;
import static org.openlca.git.repo.ExampleData.COMMIT_3;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.BinaryResolver;

public class DiffsTests extends AbstractRepositoryTests {

	@Test
	public void testNoDiffWithDatabase() throws IOException {
		repo.commit(COMMIT_1);
		repo.commit(COMMIT_2);
		repo.commit(COMMIT_3);
		Assert.assertTrue(repo.diffs.find().withDatabase().isEmpty());
	}

	@Test
	public void testDiffWithDatabase() throws IOException {
		var commits = new Commit[] {
				repo.commits.get(repo.commit(COMMIT_1)),
				repo.commits.get(repo.commit(COMMIT_2)),
				repo.commits.get(repo.commit(COMMIT_3)) };

		var diffs = repo.diffs.find().commit(commits[0]).withDatabase().stream().sorted().toList();
		Assert.assertEquals(10, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
		assertModel(DiffType.MOVED, "ACTOR/category/caa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(5));
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(6));
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(7));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(8));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(9));

		diffs = repo.diffs.find().commit(commits[1]).withDatabase().stream().sorted().toList();
		Assert.assertEquals(6, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
		assertModel(DiffType.MOVED, "ACTOR/category/caa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(5));
	}

	@Test
	public void testDiffWithDatabaseExcludeCategories() throws IOException {
		var commits = new Commit[] {
				repo.commits.get(repo.commit(COMMIT_1)),
				repo.commits.get(repo.commit(COMMIT_2)),
				repo.commits.get(repo.commit(COMMIT_3)) };

		var diffs = repo.diffs.find().commit(commits[0]).excludeCategories().withDatabase().stream().sorted().toList();
		Assert.assertEquals(9, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
		assertModel(DiffType.MOVED, "ACTOR/category/caa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(5));
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(6));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(7));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(8));
	}

	@Test
	public void testDiffWithDatabaseOnlyCategories() throws IOException {
		var commits = new Commit[] {
				repo.commits.get(repo.commit(COMMIT_1)),
				repo.commits.get(repo.commit(COMMIT_2)),
				repo.commits.get(repo.commit(COMMIT_3)) };

		var diffs = repo.diffs.find().commit(commits[0]).onlyCategories().withDatabase().stream().sorted().toList();
		Assert.assertEquals(1, diffs.size());
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(0));

		diffs = repo.diffs.find().commit(commits[1]).onlyCategories().withDatabase().stream().sorted().toList();
		Assert.assertEquals(0, diffs.size());
	}

	@Test
	public void testDiffWithCommit() throws IOException {
		var commits = new Commit[] {
				repo.commits.get(repo.commit(COMMIT_1)),
				repo.commits.get(repo.commit(COMMIT_2)),
				repo.commits.get(repo.commit(COMMIT_3)) };

		var diffs = repo.diffs.find().commit(commits[0]).with(commits[1]).stream().sorted().toList();
		Assert.assertEquals(4, diffs.size());
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(0));
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(1));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(2));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(3));

		diffs = repo.diffs.find().commit(commits[1]).with(commits[2]).stream().sorted().toList();
		Assert.assertEquals(6, diffs.size());
		assertModel(DiffType.MODIFIED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json", diffs.get(1));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json", diffs.get(2));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json", diffs.get(3));
		assertModel(DiffType.ADDED, "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json", diffs.get(4));
		assertModel(DiffType.MOVED, "ACTOR/category/caa39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(5));
	}

	@Test
	public void testDiffWithCommitExcludeCategories() throws IOException {
		var commits = new Commit[] {
				repo.commits.get(repo.commit(COMMIT_1)),
				repo.commits.get(repo.commit(COMMIT_2)),
				repo.commits.get(repo.commit(COMMIT_3)) };

		var diffs = repo.diffs.find().commit(commits[0]).excludeCategories().with(commits[1]).stream().sorted()
				.toList();
		Assert.assertEquals(3, diffs.size());
		assertModel(DiffType.DELETED, "SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(0));
		assertModel(DiffType.DELETED, "SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(1));
		assertModel(DiffType.ADDED, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", diffs.get(2));
	}

	@Test
	public void testDiffWithCommitOnlyCategories() throws IOException {
		var commits = new Commit[] {
				repo.commits.get(repo.commit(COMMIT_1)),
				repo.commits.get(repo.commit(COMMIT_2)),
				repo.commits.get(repo.commit(COMMIT_3)) };

		var diffs = repo.diffs.find().commit(commits[0]).onlyCategories().with(commits[1]).stream().sorted().toList();
		Assert.assertEquals(1, diffs.size());
		assertEmptyCategory(DiffType.DELETED, "SOURCE/c_category", diffs.get(0));

		diffs = repo.diffs.find().commit(commits[1]).onlyCategories().with(commits[2]).stream().sorted().toList();
		Assert.assertEquals(0, diffs.size());
	}

	@Test
	public void testDiffCategories() throws GitAPIException, IOException, URISyntaxException {
		repo = new TestRepository(getRemotePath());
		repo.create("UNIT_GROUP/Technical unit groups",
				"UNIT_GROUP/Economic unit groups",
				"UNIT_GROUP/Economic unit groups/Sub 1",
				"UNIT_GROUP/Economic unit groups/Sub 2",
				"FLOW_PROPERTY/Technical flow properties",
				"FLOW_PROPERTY/Economic flow properties",
				"FLOW_PROPERTY/Economic flow properties/Sub 1",
				"FLOW_PROPERTY/Economic flow properties/Sub 2");
		var diffs = repo.diffs.find().withDatabase();
		Assert.assertEquals(8, diffs.size());
		assertCategory(DiffType.ADDED, "FLOW_PROPERTY/Economic flow properties", diffs.get(0));
		assertEmptyCategory(DiffType.ADDED, "FLOW_PROPERTY/Economic flow properties/Sub 1", diffs.get(1));
		assertEmptyCategory(DiffType.ADDED, "FLOW_PROPERTY/Economic flow properties/Sub 2", diffs.get(2));
		assertEmptyCategory(DiffType.ADDED, "FLOW_PROPERTY/Technical flow properties", diffs.get(3));
		assertCategory(DiffType.ADDED, "UNIT_GROUP/Economic unit groups", diffs.get(4));
		assertEmptyCategory(DiffType.ADDED, "UNIT_GROUP/Economic unit groups/Sub 1", diffs.get(5));
		assertEmptyCategory(DiffType.ADDED, "UNIT_GROUP/Economic unit groups/Sub 2", diffs.get(6));
		assertEmptyCategory(DiffType.ADDED, "UNIT_GROUP/Technical unit groups", diffs.get(7));
	}

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}

	private void assertModel(DiffType expectedType, String expectedPath, Diff diff) {
		Assert.assertEquals(expectedType, diff.diffType);
		Assert.assertEquals(expectedPath, diff.path);
		Assert.assertFalse(diff.isCategory);
		Assert.assertFalse(diff.isEmptyCategory);
	}

	private void assertCategory(DiffType expectedType, String expectedPath, Diff diff) {
		Assert.assertEquals(expectedType, diff.diffType);
		Assert.assertEquals(expectedPath, diff.path);
		Assert.assertTrue(diff.isCategory);
		Assert.assertFalse(diff.isEmptyCategory);
	}

	private void assertEmptyCategory(DiffType expectedType, String expectedPath, Diff diff) {
		Assert.assertEquals(expectedType, diff.diffType);
		Assert.assertEquals(expectedPath, diff.path);
		Assert.assertTrue(diff.isCategory);
		Assert.assertTrue(diff.isEmptyCategory);
	}
}
