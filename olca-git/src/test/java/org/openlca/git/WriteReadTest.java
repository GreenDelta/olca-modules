package org.openlca.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.model.Change;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.jsonld.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WriteReadTest {

	@Test
	public void testSimpleDataSet() throws Exception {
		try (var tmp = TmpConfig.create();
				var repo = tmp.repo()) {

			// expect an empty database and repo
			assertTrue(repo.diffs.find().withDatabase().isEmpty());

			// insert model in database
			var unitGroup = UnitGroup.of("Units of mass", "kg");
			unitGroup.category = CategoryDao.sync(
					repo.database, ModelType.UNIT_GROUP, "Technical unit groups");
			repo.database.insert(unitGroup);
			repo.descriptors.reload();

			// should find 1 diff
			var diffs = Change.of(repo.diffs.find().excludeCategories().withDatabase());
			assertEquals(1, diffs.size());

			// commit it
			var writer = new DbCommitWriter(repo)
					.as(tmp.committer());
			var commitId = writer.write("initial commit", diffs);
			repo.index.reload();

			// get the data set from the repo
			var ref = repo.references.get(ModelType.UNIT_GROUP, unitGroup.refId, commitId);
			var string = repo.datasets.get(ref);
			var jsonObj = new Gson().fromJson(string, JsonObject.class);
			assertEquals(unitGroup.refId, Json.getString(jsonObj, "@id"));

			// make sure that we can find the commit
			var commit = repo.commits.get(commitId);
			assertEquals("initial commit", commit.message);
		}
	}

}
