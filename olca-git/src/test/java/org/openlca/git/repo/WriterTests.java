package org.openlca.git.repo;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.Tests;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.TreeValidator;
import org.openlca.git.model.Change;
import org.openlca.git.model.ModelRef;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.util.Strings;

public class WriterTests {

	@Test
	public void testCommit() throws IOException {
		var paths = Arrays.asList(
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree");
		try (var config = TmpConfig.create()) {
			var repo = config.repo();

			// create models in database
			paths.forEach(path -> Tests.create(repo.database, path));
			repo.descriptors.reload();
			var changes = paths.stream()
					.map(ModelRef::new)
					.map(Change::add)
					.collect(Collectors.toList());

			// write commit to repo
			var writer = new DbCommitWriter(repo);
			var commitId = writer.write("initial commit", changes);
			Assert.assertNotNull(commitId);

			// validate tree structure
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, null),
					"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "ACTOR"),
					"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"category");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "ACTOR/category"),
					"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "FLOW"),
					"cat");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE"),
					"a_category",
					"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
					"c_category",
					"category_one",
					"category_two",
					"category_zhree");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/a_category"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/c_category"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/category_one"),
					"aca39f5b-5021-4b6b-9330-739f082dfae0.json",
					"aca49f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/category_two"),
					"0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/category_zhree"),
					".empty");

			changes = Arrays.asList(
					// delete empty category
					Change.delete(new ModelRef("SOURCE/c_category")),
					// delete one of several data sets in a category
					Change.delete(new ModelRef("SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
					// delete last data set in a category (must create .empty)
					Change.delete(new ModelRef("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
					// add data set in empty category (must delete .empty)
					Change.add(new ModelRef("SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json")));

			// create new and delete changes in db
			repo.database.delete(new CategoryDao(repo.database).getForPath(ModelType.SOURCE, "c_category"));
			repo.database.delete(repo.database.get(Source.class, "aca49f5b-5021-4b6b-9330-739f082dfae0"));
			repo.database.delete(repo.database.get(Source.class, "0ca39f5b-5021-4b6b-9330-739f082dfae0"));
			Tests.create(repo.database, "SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
			repo.descriptors.reload();

			// write changes to repo
			commitId = writer.write("delete and add some data", changes);
			Assert.assertNotNull(commitId);

			// validate tree structure
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, null),
					"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "ACTOR"),
					"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"category");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "ACTOR/category"),
					"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "FLOW"),
					"cat");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE"),
					"a_category",
					"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
					"category_one",
					"category_two",
					"category_zhree");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/a_category"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/category_one"),
					"aca39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/category_two"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitId, "SOURCE/category_zhree"),
					"fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		}
	}

	private AbstractTreeIterator createIterator(ClientRepository repo, String commitId, String path)
			throws IOException {
		var commit = repo.parseCommit(ObjectId.fromString(commitId));
		if (commit == null)
			return new EmptyTreeIterator();
		var treeId = Strings.nullOrEmpty(path)
				? commit.getTree().getId()
				: repo.getSubTreeId(commit.getTree().getId(), path);
		if (ObjectId.zeroId().equals(treeId))
			return new EmptyTreeIterator();
		var it = new CanonicalTreeParser();
		it.reset(repo.newObjectReader(), treeId);
		return it;
	}

}
