package org.openlca.git;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.git.find.Commits;
import org.openlca.git.find.Datasets;
import org.openlca.git.model.Diff;
import org.openlca.git.util.DiffEntries;
import org.openlca.git.util.Repositories;
import org.openlca.git.writer.CommitWriter;
import org.openlca.jsonld.Json;
import org.openlca.util.Dirs;

public class WriteReadTest {

	private final IDatabase db = Tests.db();

	@Test
	public void testSimpleDataSet() throws Exception {
		var temp = TempConfig.create();

		// expect an empty database and repo
		assertTrue(DiffEntries.workspace(temp.config).isEmpty());

		// insert model in database
		var unitGroup = UnitGroup.of("Units of mass", "kg");
		unitGroup.category = CategoryDao.sync(
			db, ModelType.UNIT_GROUP, "Technical unit groups");
		db.insert(unitGroup);

		// should find 1 diff
		var diffs = DiffEntries.workspace(temp.config)
			.stream()
			.map(Diff::new)
			.toList();
		assertEquals(1, diffs.size());

		// commit it
		var writer = new CommitWriter(temp.config);
		var commitId = writer.commit("initial commit", diffs);

		// get the data set from the repo
		var id = temp.ids().get(unitGroup);
		var string = new Datasets(temp.repo()).get(id);
		var jsonObj = new Gson().fromJson(string, JsonObject.class);
		assertEquals(unitGroup.refId, Json.getString(jsonObj, "@id"));

		// make sure that we can find the commit
		var commit = new Commits(temp.repo()).get(commitId);
		assertEquals("initial commit", commit.message);

		temp.delete();
	}

	private record TempConfig(Config config, File dir) {

		static TempConfig create() {
			try {
				var dir = Files.createTempDirectory("olca-git-test").toFile();
				var repo = Repositories.open(new File(dir, "repo"));
				var idStore = ObjectIdStore.openJson(new File(dir, "id-store"));
				var committer = new PersonIdent("user", "user@example.com");
				var config =  Config.newJsonConfig(Tests.db(), idStore, repo, committer);
				return new TempConfig(config, dir);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		FileRepository repo() {
			return config.repo;
		}

		ObjectIdStore ids() {
			return config.store;
		}

		void delete() {
			config.repo.close();
			Dirs.delete(dir);
		}
	}

}
