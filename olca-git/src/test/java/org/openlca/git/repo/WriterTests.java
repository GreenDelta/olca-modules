package org.openlca.git.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Change;
import org.openlca.git.model.ModelRef;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.jsonld.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WriterTests extends AbstractRepositoryTests {

	@Test
	public void testCommit() throws IOException {
		// first commit
		var commitId1 = repo.commit(COMMIT_1);
		Assert.assertNotNull(commitId1);
		repo.assertEqual(repo.createIterator(commitId1),
				"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
		repo.assertEqual(repo.createIterator(commitId1, "ACTOR"),
				"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"cAA39f5b-5021_bin1.json+39f082dfae0._bin",
				"category");
		repo.assertEqual(repo.createIterator(commitId1, "ACTOR/category"),
				"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqual(repo.createIterator(commitId1, "FLOW"),
				"cat");
		repo.assertEqual(repo.createIterator(commitId1, "SOURCE"),
				"a_category",
				"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"c_category",
				"category_one",
				"category_two",
				"category_zhree");
		repo.assertEqual(repo.createIterator(commitId1, "SOURCE/a_category"),
				".empty");
		repo.assertEqual(repo.createIterator(commitId1, "SOURCE/c_category"),
				".empty");
		repo.assertEqual(repo.createIterator(commitId1, "SOURCE/category_one"),
				"a.json",
				"aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqual(repo.createIterator(commitId1, "SOURCE/category_two"),
				"0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqual(repo.createIterator(commitId1, "SOURCE/category_zhree"),
				".empty");

		// second commit
		var commitId2 = repo.commit(COMMIT_2);
		Assert.assertNotNull(commitId2);
		repo.assertEqual(repo.createIterator(commitId2),
				"ACTOR", "FLOW", "SOURCE", RepositoryInfo.FILE_NAME);
		repo.assertEqual(repo.createIterator(commitId2, "ACTOR"),
				"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"cAA39f5b-5021_bin1.json+39f082dfae0._bin",
				"category");
		repo.assertEqual(repo.createIterator(commitId2, "ACTOR/category"),
				"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqual(repo.createIterator(commitId2, "FLOW"),
				"cat");
		repo.assertEqual(repo.createIterator(commitId2, "SOURCE"),
				"a_category",
				"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"category_one",
				"category_two",
				"category_zhree");
		repo.assertEqual(repo.createIterator(commitId2, "SOURCE/a_category"),
				".empty");
		repo.assertEqual(repo.createIterator(commitId2, "SOURCE/category_one"),
				"a.json");
		repo.assertEqual(repo.createIterator(commitId2, "SOURCE/category_two"),
				".empty");
		repo.assertEqual(repo.createIterator(commitId2, "SOURCE/category_zhree"),
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
		repo.commit(changes);
	}

	@Test
	public void testMergeKeepRemote() throws IOException {
		var refId = "0aa39f5b-5021-4b6b-9330-739f082dfae0";
		var changes = Arrays.asList(Change.add(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId1 = repo.commit(changes);
		Assert.assertNotNull(commitId1);
		
		var commit = repo.commits.get(commitId1);
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId2 = repo.commit(commit, changes);
		Assert.assertNotNull(commitId2);
		
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId3 = repo.commit(commit, changes);
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
		var commitId1 = repo.commit(changes);
		Assert.assertNotNull(commitId1);
		var commit = repo.commits.get(commitId1);
		
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId2 = repo.commit(commit, changes);
		Assert.assertNotNull(commitId2);
		
		changes = Arrays.asList(Change.modify(new ModelRef("ACTOR/" + refId + ".json")));
		var commitId3 = repo.commit(commit, changes);
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

	@Test
	public void testSimpleDataSet() throws Exception {
		// expect an empty database and repo
		assertTrue(repo.diffs.find().withDatabase().isEmpty());

		// insert model in database
		var refId = "bca39f5b-5021-4b6b-9330-739f082dfae0";
		var category = "Technical unit groups";
		repo.create("UNIT_GROUP/" + category,
				"UNIT_GROUP/" + category + "/" + refId + ".json");

		// should find 1 diff without categories
		var diffs = Change.of(repo.diffs.find().excludeCategories().withDatabase());
		assertEquals(1, diffs.size());

		// should find 1 category diff
		diffs = Change.of(repo.diffs.find().onlyCategories().withDatabase());
		assertEquals(1, diffs.size());

		// should find 2 total diffs
		diffs = Change.of(repo.diffs.find().withDatabase());
		assertEquals(2, diffs.size());

		// commit it
		var writer = new DbCommitWriter(repo)
				.as(committer);
		var commitId = writer.write("initial commit", diffs);
		repo.index.reload();

		// get the data set from the repo
		var ref = repo.references.get(ModelType.UNIT_GROUP, refId, commitId);
		var string = repo.datasets.get(ref);
		var jsonObj = new Gson().fromJson(string, JsonObject.class);
		assertEquals(UnitGroup.class.getSimpleName(), Json.getString(jsonObj, "@type"));
		assertEquals(refId, Json.getString(jsonObj, "@id"));
		assertEquals(category, Json.getString(jsonObj, "category"));

		// make sure that we can find the commit
		var commit = repo.commits.get(commitId);
		assertEquals("initial commit", commit.message);
	}

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}

}
