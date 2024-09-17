package org.openlca.git.iterator;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Change;
import org.openlca.git.writer.DatabaseBinaryResolver;

public class ChangeIteratorTests extends AbstractRepositoryTests {

	@Test
	public void testAddDataAndEmptyCategories() throws IOException {
		repo.create("ACTOR/0AA39f5b.5021_4b6b-dd90.739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree");
		var changes = Change.of(repo.diffs.find().withDatabase());
		Collections.shuffle(changes);
		var iterator = new ChangeIterator(repo, null, new DatabaseBinaryResolver(repo.database), changes);
		repo.assertEqualRecursive(iterator, "ACTOR/0AA39f5b.5021_4b6b-dd90.739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/a_category/.empty",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category/.empty",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/.empty",
				RepositoryInfo.FILE_NAME);
	}

	@Test
	public void testDeleteLastElementAndAddInEmptyCategory() throws IOException {
		repo.create("ACTOR/0AA39f5b.5021_4b6b-dd90.739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree");
		var commitId = repo.commitWorkspace();
		repo.delete("SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.create("SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		var changes = Change.of(repo.diffs.find().withDatabase());
		var iterator = new ChangeIterator(repo, commitId, new DatabaseBinaryResolver(repo.database), changes);
		repo.assertEqualRecursive(iterator,
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/.empty",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/.empty",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME);
	}

	@Test
	public void testMove() throws IOException {
		repo.create("ACTOR/category/0AA39f5b.5021_4b6b-dd90.739f082dfae0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		var commitId = repo.commitWorkspace();
		repo.move("ACTOR/category/0AA39f5b.5021_4b6b-dd90.739f082dfae0.json", "category2");
		repo.move("ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json", "category");
		var changes = Change.of(repo.diffs.find().withDatabase());
		var iterator = new ChangeIterator(repo, commitId, new DatabaseBinaryResolver(repo.database), changes);
		repo.assertEqualRecursive(iterator,
				"ACTOR/category/0AA39f5b.5021_4b6b-dd90.739f082dfae0.json", // deleted
				"ACTOR/category/1aa39f5b-5021-4b6b-9330-739f082dfae0.json", // added
				"ACTOR/category2/0AA39f5b.5021_4b6b-dd90.739f082dfae0.json", // added
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json", // deleted
				RepositoryInfo.FILE_NAME);
	}

}
