package org.openlca.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.git.model.Change;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.jsonld.Json;
import org.openlca.util.Dirs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WriteReadTest {

	private final IDatabase db = Tests.db();

	@Test
	public void testSimpleDataSet() throws Exception {
		try (var tmp = TmpConfig.create()) {

			// expect an empty database and repo
			assertTrue(tmp.repo.diffs.find().withDatabase().isEmpty());

			// insert model in database
			var unitGroup = UnitGroup.of("Units of mass", "kg");
			unitGroup.category = CategoryDao.sync(
					db, ModelType.UNIT_GROUP, "Technical unit groups");
			db.insert(unitGroup);

			// should find 1 diff
			var diffs = tmp.repo.diffs.find().excludeCategories().withDatabase().stream()
					.map(Change::new)
					.collect(Collectors.toList());
			assertEquals(1, diffs.size());

			// commit it
			var writer = new DbCommitWriter(tmp.repo)
					.as(tmp.committer);
			var commitId = writer.write("initial commit", diffs);

			// get the data set from the repo
			var ref = tmp.repo.references.get(ModelType.UNIT_GROUP, unitGroup.refId, commitId);
			var string = tmp.repo.datasets.get(ref);
			var jsonObj = new Gson().fromJson(string, JsonObject.class);
			assertEquals(unitGroup.refId, Json.getString(jsonObj, "@id"));

			// make sure that we can find the commit
			var commit = tmp.repo.commits.get(commitId);
			assertEquals("initial commit", commit.message);
		}
	}

	private record TmpConfig(ClientRepository repo, PersonIdent committer,
			File dir) implements AutoCloseable {

		static TmpConfig create() {
			try {
				var dir = Files.createTempDirectory("olca-git-test").toFile();
				var repo = new ClientRepository(new File(dir, "repo"), Tests.db());
				repo.create(true);
				return new TmpConfig(repo, new PersonIdent("user", "user@example.com"), dir);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close() {
			repo.close();
			try {
				Dirs.delete(dir);
			} catch (Exception e) {
				// fail silent
			}
		}

	}

}
