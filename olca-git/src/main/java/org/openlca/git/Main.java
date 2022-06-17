package org.openlca.git;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.openlca.core.DataDir;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Change;
import org.openlca.git.util.Diffs;
import org.openlca.git.writer.CommitWriter;
import org.openlca.util.Categories;

import com.google.common.io.Files;

public class Main {

	private static final String db = "test_elem";
	// private static final String db = "ecoinvent_36_cutoff_unit_20200512";
	private static final PersonIdent committer = new PersonIdent("greve", "greve@greendelta.com");
	private static final File repoDir = new File("C:/Users/Sebastian/test/olca-git/" + db);
	private static final File tmp = new File("C:/Users/Sebastian/test/tmp");

	public static void main(String[] args) throws IOException {
		var dbDir = new File(DataDir.get().getDatabasesDir(), db);
		if (tmp.exists()) {
			delete(tmp);
		}
		copy(dbDir, tmp);
		try (var database = new Derby(tmp);
				var repo = new FileRepository(repoDir)) {
			var storeFile = new File(tmp, "object-id.store");
			var idStore = ObjectIdStore.fromFile(storeFile);
			var writer = new DbWriter(repo, database, idStore);

			if (repoDir.exists()) {
				delete(repoDir);
			}

			repo.create(true);
			writer.refData(false);

			// writer.update();
			// writer.delete();
		}
	}

	private static void copy(File from, File to) throws IOException {
		if (from.isDirectory()) {
			to.mkdirs();
			for (File child : from.listFiles()) {
				copy(child, new File(to, child.getName()));
			}
			return;
		}
		Files.copy(from, to);
	}

	private static void delete(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				delete(child);
			}
		}
		file.delete();
	}

	private static class DbWriter {

		private static final ModelType[] REF_DATA_TYPES = {
				ModelType.ACTOR,
				ModelType.SOURCE,
				ModelType.UNIT_GROUP,
				ModelType.FLOW_PROPERTY,
				ModelType.CURRENCY,
				ModelType.LOCATION,
				ModelType.DQ_SYSTEM,
				ModelType.FLOW,
				ModelType.PROCESS,
				ModelType.IMPACT_CATEGORY,
				ModelType.IMPACT_METHOD
		};
		private final Repository repo;
		private final IDatabase database;
		private final ObjectIdStore idStore;
		private final CommitWriter writer;

		private DbWriter(Repository repo, IDatabase database, ObjectIdStore idStore) {
			this.repo = repo;
			this.database = database;
			this.idStore = idStore;
			this.writer = new CommitWriter(repo, database).as(committer).saveIdsIn(idStore);
		}

		private void refData(boolean singleCommit) throws IOException {
			if (singleCommit) {
				refDataSingleCommit();
			} else {
				refDataSeparateCommits();
			}
		}

		private void refDataSingleCommit() throws IOException {
			var changes = Diffs.of(repo).with(database, idStore).stream()
					.map(Change::new)
					.collect(Collectors.toList());
			System.out.println(writer.write("Added data", changes));
		}

		private void refDataSeparateCommits() throws IOException {
			var changes = Diffs.of(repo).with(database, idStore).stream().map(Change::new)
					.collect(Collectors.toList());
			long time = 0;
			for (ModelType type : REF_DATA_TYPES) {
				var filtered = changes.stream()
						.filter(d -> d.path.startsWith(type.name() + "/"))
						.toList();
				long t = System.currentTimeMillis();
				System.out.println("Committing " + filtered.size() + " files");
				System.out.println(writer.write("Added data for type " + type.name(), filtered));
				time += System.currentTimeMillis() - t;
			}
			System.out.println("Total time: " + time + "ms");
		}

		private void update() throws IOException {
			var dao = new LocationDao(database);

			var deleted = dao.getAll().get(5);
			dao.delete(deleted);
			idStore.remove(deleted);

			var changed = dao.getAll().get(0);
			changed.description = "changed " + Math.random();
			dao.update(changed);
			idStore.remove(changed);

			var newLoc = new Location();
			newLoc.refId = UUID.randomUUID().toString();
			newLoc.name = "new";
			dao.insert(newLoc);
			idStore.save();

			var changes = Diffs.of(repo).with(database, idStore).stream()
					.map(Change::new)
					.collect(Collectors.toList());
			var writer = new CommitWriter(repo, database).as(committer).saveIdsIn(idStore);
			System.out.println(writer.write("Updated data", changes));
		}

		private void delete() throws IOException {
			var categoryPath = Categories.pathsOf(database);
			for (var type : REF_DATA_TYPES) {
				for (var d : Daos.root(database, type).getDescriptors()) {
					idStore.remove(categoryPath, d);
				}
				if (type == ModelType.CURRENCY) {
					var dao = new CurrencyDao(database);
					var currencies = dao.getAll();
					for (var currency : currencies) {
						currency.referenceCurrency = null;
						dao.update(currency);
					}
					dao.deleteAll();
				} else {
					Daos.root(database, type).deleteAll();
				}
			}
			idStore.save();
			var changes = Diffs.of(repo).with(database, idStore).stream()
					.map(Change::new)
					.collect(Collectors.toList());
			var writer = new CommitWriter(repo, database).as(committer).saveIdsIn(idStore);
			System.out.println(writer.write("Deleted data", changes));
		}

	}

}
