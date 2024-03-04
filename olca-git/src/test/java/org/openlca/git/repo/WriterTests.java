package org.openlca.git.repo;

import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.TreeValidator;
import org.openlca.git.model.Change;
import org.openlca.git.model.ModelRef;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.util.Strings;

public class WriterTests extends AbstractRepositoryTests {

	@Test
	public void testCommit() throws IOException {
		// first commit
		var commitId1 = commit(COMMIT_1);
		Assert.assertNotNull(commitId1);
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, null),
				"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "ACTOR"),
				"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"caa39f5b-5021-4b6b-9330-739f082dfae0_bin",
				"category");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "ACTOR/category"),
				"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "FLOW"),
				"cat");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "SOURCE"),
				"a_category",
				"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"c_category",
				"category_one",
				"category_two",
				"category_zhree");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "SOURCE/a_category"),
				".empty");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "SOURCE/c_category"),
				".empty");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "SOURCE/category_one"),
				"aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "SOURCE/category_two"),
				"0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId1, "SOURCE/category_zhree"),
				".empty");

		// second commit
		var commitId2 = commit(COMMIT_2);
		Assert.assertNotNull(commitId2);
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, null),
				"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "ACTOR"),
				"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"caa39f5b-5021-4b6b-9330-739f082dfae0_bin",
				"category");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "ACTOR/category"),
				"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "FLOW"),
				"cat");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "SOURCE"),
				"a_category",
				"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"category_one",
				"category_two",
				"category_zhree");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "SOURCE/a_category"),
				".empty");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "SOURCE/category_one"),
				"aca39f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "SOURCE/category_two"),
				".empty");
		TreeValidator.assertEqual(repo, createIterator(repo, commitId2, "SOURCE/category_zhree"),
				"fca39f5b-5021-4b6b-9330-739f082dfae0.json");
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
		commit(changes);
	}

	@Test
	public void testMergeKeepRemote() throws IOException {
		var refId = "0aa39f5b-5021-4b6b-9330-739f082dfae0";
		var changes = Arrays.asList(Change.add(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId1 = commit(changes);
		Assert.assertNotNull(commitId1);
		var commit = repo.commits.get(commitId1);
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId2 = commit(commit, changes);
		Assert.assertNotNull(commitId2);
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId3 = commit(commit, changes);
		Assert.assertNotNull(commitId3);
		var writer = new DbCommitWriter(repo, new StaticBinaryResolver(ExampleData.PATH_TO_BINARY));
		writer.merge(commitId2, commitId3);
		var mergeCommitId = writer.write("merge commit", new ArrayList<>());
		Assert.assertNotNull(mergeCommitId);
		var ref = repo.references.get(ModelType.ACTOR, refId, mergeCommitId);
		var version = repo.datasets.getVersionAndLastChange(ref).get("version");
		Assert.assertEquals("00.00.002", version);
	}

	@Test
	public void testMergeKeepLocal() throws IOException {
		var refId = "0aa39f5b-5021-4b6b-9330-739f082dfae0";
		var changes = Arrays.asList(Change.add(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId1 = commit(changes);
		Assert.assertNotNull(commitId1);
		var commit = repo.commits.get(commitId1);
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId2 = commit(commit, changes);
		Assert.assertNotNull(commitId2);
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId3 = commit(commit, changes);
		Assert.assertNotNull(commitId3);
		var actor = repo.database.get(Actor.class, refId);
		actor.version = Version.valueOf(0, 0, 1);
		repo.database.update(actor);
		var writer = new DbCommitWriter(repo, new StaticBinaryResolver(ExampleData.PATH_TO_BINARY));
		writer.merge(commitId2, commitId3);
		var mergeCommitId = writer.write("merge commit", changes);
		Assert.assertNotNull(mergeCommitId);
		var ref = repo.references.get(ModelType.ACTOR, refId, mergeCommitId);
		var version = repo.datasets.getVersionAndLastChange(ref).get("version");
		Assert.assertEquals("00.00.001", version);
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

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}

}
