package org.openlca.git.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Version;
import org.openlca.git.Tests;
import org.openlca.git.model.Change;
import org.openlca.git.model.Change.ChangeType;
import org.openlca.git.model.Commit;
import org.openlca.git.model.ModelRef;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.writer.DbCommitWriter;

class RepoUtil {

	static final Map<String, List<String>> PATH_TO_BINARY = Map.of(
			"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json", Arrays.asList("test.txt"));

	static final List<Change> EXAMPLE_COMMIT_1 = Arrays.asList(
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/a_category")),
			Change.add(new ModelRef("SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/c_category")),
			Change.add(new ModelRef("SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/category_zhree")));

	static final List<Change> EXAMPLE_COMMIT_2 = Arrays.asList(
			// delete empty category
			Change.delete(new ModelRef("SOURCE/c_category")),
			// delete one of several data sets in a category
			Change.delete(new ModelRef("SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
			// delete last data set in a category (must create .empty)
			Change.delete(new ModelRef("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			// add data set in empty category (must delete .empty)
			Change.add(new ModelRef("SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json")));

	static final List<Change> EXAMPLE_COMMIT_3 = Arrays.asList(
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json")),
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json")),
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json")),
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json")));

	static String commit(ClientRepository repo, List<Change> changes) throws IOException {
		return commit(repo, null, changes);
	}

	static String commit(ClientRepository repo, Commit reference, List<Change> changes) throws IOException {
		// create and delete models in database
		var categoryDao = new CategoryDao(repo.database);
		changes.forEach(change -> {
			if (change.changeType == ChangeType.ADD) {
				Tests.create(repo.database, change.path);
			} else if (change.changeType == ChangeType.MODIFY) {
				var model = repo.database.get(change.type.getModelClass(), change.refId);
				var version = new Version(model.version);
				version.incUpdate();
				model.version = version.getValue();
				repo.database.update(model);
			} else if (change.changeType == ChangeType.DELETE) {
				if (change.isCategory) {
					repo.database.delete(categoryDao.getForPath(change.type, change.getCategoryPath()));
				} else {
					repo.database.delete(repo.database.get(change.type.getModelClass(), change.refId));
				}
			}
		});
		repo.descriptors.reload();
		// write commit to repo
		var writer = new DbCommitWriter(repo, new StaticBinaryResolver());
		writer.reference(reference);
		return writer.write("commit", changes);
	}

	static class StaticBinaryResolver implements BinaryResolver {

		@Override
		public List<String> list(Change change, String relativePath) {
			if (!PATH_TO_BINARY.containsKey(change.path))
				return new ArrayList<>();
			return new ArrayList<>(PATH_TO_BINARY.get(change.path));
		}

		@Override
		public boolean isDirectory(Change change, String relativePath) {
			return false;
		}

		@Override
		public byte[] resolve(Change change, String relativePath) throws IOException {
			return ("Test - " + relativePath).getBytes();
		}

	}

}
