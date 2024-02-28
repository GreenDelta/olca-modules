package org.openlca.git.iterator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Test;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.Tests;
import org.openlca.git.TreeValidator;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.model.Change;
import org.openlca.git.model.ModelRef;
import org.openlca.git.writer.DatabaseBinaryResolver;
import org.openlca.git.writer.DbCommitWriter;

public class ChangeIteratorTests {

	@Test
	public void testAddDataAndEmptyCategories() throws IOException {
		var paths = Arrays.asList(
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/a_category", // expect SOURCE/a_category/.empty
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category", // expect SOURCE/c_category/.empty
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree"); // expect SOURCE/category_zhree/.empty
		try (var repo = TmpConfig.create().repo()) {
			paths.forEach(path -> Tests.create(repo.database, path));
			repo.descriptors.reload();
			var changes = paths.stream()
					.map(ModelRef::new)
					.map(Change::add)
					.collect(Collectors.toList());
			Collections.shuffle(changes);
			var iterator = new ChangeIterator(repo, null, new DatabaseBinaryResolver(repo.database), changes);
			var expected = paths.toArray(new String[paths.size() + 1]);
			for (var i : Arrays.asList(4, 6, 10)) {
				expected[i] += "/.empty";
			}
			expected[expected.length - 1] = RepositoryInfo.FILE_NAME;
			TreeValidator.assertEqualRecursive(repo, iterator, expected);
		}
	}

	@Test
	public void testDeleteLastElementAndAddInEmptyCategory() throws IOException {
		var paths = Arrays.asList(
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree");
		try (var config = TmpConfig.create()) {
			var repo = config.repo();
			paths.forEach(path -> Tests.create(repo.database, path));
			repo.descriptors.reload();
			var changes = paths.stream()
					.map(ModelRef::new)
					.map(Change::add)
					.collect(Collectors.toList());
			var writer = new DbCommitWriter(repo);
			var commitId = writer.as(config.committer()).write("initial commit", changes);
			changes = Arrays.asList(
					Change.delete(new ModelRef("SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
					Change.delete(new ModelRef("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
					Change.add(new ModelRef("SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json")));
			var iterator = new ChangeIterator(repo, commitId, new DatabaseBinaryResolver(repo.database), changes);
			var expected = new String[] {
					"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json", // DELETED
					"SOURCE/category_two/.empty", // Add empty flag
					"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json", // DELETED
					"SOURCE/category_zhree/.empty", // Remove empty flag
					"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json", // ADDED
					RepositoryInfo.FILE_NAME
			};
			TreeValidator.assertEqualRecursive(repo, iterator, expected);

		}
	}

}
