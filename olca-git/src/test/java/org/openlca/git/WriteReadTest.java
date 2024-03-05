package org.openlca.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.git.model.Change;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.jsonld.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WriteReadTest extends AbstractRepositoryTests {

	@Test
	public void testSimpleDataSet() throws Exception {
		// expect an empty database and repo
		assertTrue(repo.diffs.find().withDatabase().isEmpty());

		// insert model in database
		var refId = "bca39f5b-5021-4b6b-9330-739f082dfae0";
		var category = "Technical unit groups";
		create("UNIT_GROUP/" + category,
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

}
