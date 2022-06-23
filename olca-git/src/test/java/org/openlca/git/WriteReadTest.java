package org.openlca.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.git.find.Commits;
import org.openlca.git.find.Datasets;
import org.openlca.git.model.Change;
import org.openlca.git.util.Diffs;
import org.openlca.git.util.Repositories;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.jsonld.Json;
import org.openlca.util.Dirs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WriteReadTest {

	private final IDatabase db = Tests.db();

	@Test
	public void testSimpleDataSet() throws Exception {
		var tmp = TmpConfig.create();

		// expect an empty database and repo
		assertTrue(Diffs.of(tmp.repo).with(tmp.database, tmp.idStore).isEmpty());

		// insert model in database
		var unitGroup = UnitGroup.of("Units of mass", "kg");
		unitGroup.category = CategoryDao.sync(
				db, ModelType.UNIT_GROUP, "Technical unit groups");
		db.insert(unitGroup);

		// should find 1 diff
		var diffs = Diffs.of(tmp.repo).with(tmp.database, tmp.idStore).stream()
				.map(Change::new)
				.collect(Collectors.toList());
		assertEquals(1, diffs.size());

		// commit it
		var writer = new DbCommitWriter(tmp.repo, tmp.database).saveIdsIn(tmp.idStore).as(tmp.committer);
		var commitId = writer.write("initial commit", diffs);

		// get the data set from the repo
		var id = tmp.idStore().get(unitGroup);
		var string = Datasets.of(tmp.repo()).get(id);
		var jsonObj = new Gson().fromJson(string, JsonObject.class);
		assertEquals(unitGroup.refId, Json.getString(jsonObj, "@id"));

		// make sure that we can find the commit
		var commit = Commits.of(tmp.repo()).get(commitId);
		assertEquals("initial commit", commit.message);

		tmp.delete();
	}

	private record TmpConfig(Repository repo, IDatabase database, ObjectIdStore idStore, PersonIdent committer,
			File dir) {

		static TmpConfig create() {
			try {
				var dir = Files.createTempDirectory("olca-git-test").toFile();
				var repo = Repositories.open(new File(dir, "repo"));
				var idStore = ObjectIdStore.fromFile(new File(dir, "id-store"));
				return new TmpConfig(repo, Tests.db(), idStore, new PersonIdent("user", "user@example.com"), dir);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		void delete() {
			repo.close();
			try {
				Dirs.delete(dir);
			} catch (Exception e) {
				// fail silent
			}
		}
	}

}
