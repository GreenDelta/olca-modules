package org.openlca.git.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.TreeValidator;
import org.openlca.git.model.Change;
import org.openlca.git.model.ModelRef;
import org.openlca.util.Strings;

public class WriterTests {

	@Test
	public void testCommit() throws IOException {
		try (var config = TmpConfig.create()) {
			var repo = config.repo();
			var commitIds = SampleRepo.create(repo, SampleRepo.EXAMPLE_COMMIT_1, SampleRepo.EXAMPLE_COMMIT_2);
			Assert.assertEquals(2, commitIds.length);
			Assert.assertNotNull(commitIds[0]);
			Assert.assertNotNull(commitIds[1]);

			// validate first commit
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], null),
					"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "ACTOR"),
					"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"caa39f5b-5021-4b6b-9330-739f082dfae0_bin",
					"category");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "ACTOR/category"),
					"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "FLOW"),
					"cat");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "SOURCE"),
					"a_category",
					"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
					"c_category",
					"category_one",
					"category_two",
					"category_zhree");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "SOURCE/a_category"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "SOURCE/c_category"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "SOURCE/category_one"),
					"aca39f5b-5021-4b6b-9330-739f082dfae0.json",
					"aca49f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "SOURCE/category_two"),
					"0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[0], "SOURCE/category_zhree"),
					".empty");

			// validate second commit
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], null),
					"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "ACTOR"),
					"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
					"caa39f5b-5021-4b6b-9330-739f082dfae0_bin",
					"category");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "ACTOR/category"),
					"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "FLOW"),
					"cat");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "SOURCE"),
					"a_category",
					"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
					"category_one",
					"category_two",
					"category_zhree");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "SOURCE/a_category"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "SOURCE/category_one"),
					"aca39f5b-5021-4b6b-9330-739f082dfae0.json");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "SOURCE/category_two"),
					".empty");
			TreeValidator.assertEqual(repo, createIterator(repo, commitIds[1], "SOURCE/category_zhree"),
					"fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		}
	}

	@Test(timeout = 10000)
	public void testDotCase() throws IOException {
		// different sorting in Converter (via ModelRef.compareTo) and
		// ChangeIterator (via TreeEntry.compareTo) lead to the commit writing
		// to get stuck; FIX: encode string in ModelRef.compareTo
		var changes = new ArrayList<Change>();
		for (var i = 0; i < 30; i++) {
			changes.add(Change.add(new ModelRef(
					"FLOW/Emissions to air/indoor/" + UUID.randomUUID().toString() + ".json")));
		}
		for (var i = 0; i < 30; i++) {
			changes.add(Change.add(new ModelRef(
					"FLOW/Emissions to air/low. pop., long-term/" + UUID.randomUUID().toString() + ".json")));
		}
		for (var i = 0; i < 30; i++) {
			changes.add(Change.add(new ModelRef(
					"FLOW/Emissions to air/low. pop./" + UUID.randomUUID().toString() + ".json")));
		}
		try (var config = TmpConfig.create()) {
			var repo = config.repo();
			SampleRepo.create(repo, changes);
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
