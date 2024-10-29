package org.openlca.git.repo;

import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;
import static org.openlca.git.repo.ExampleData.COMMIT_3;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Change;
import org.openlca.git.model.ModelRef;
import org.openlca.git.repo.Entries.Find;
import org.openlca.git.util.BinaryResolver;

public class EntriesTests extends AbstractRepositoryTests {

	@Test
	public void testCountRecursive() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		Assert.assertEquals(19, count(commitIds[0]));
		Assert.assertEquals(17, count(commitIds[1]));
		Assert.assertEquals(21, count(commitIds[2]));
		Assert.assertEquals(21, repo.entries.find().recursive().count());
	}

	private long count(String commitId) {
		return repo.entries.find().recursive().commit(commitId).count();
	}

	@Test
	public void testIterate() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(repo.entries.find().commit(commitIds[0]),
				"ACTOR",
				"FLOW",
				"SOURCE");
		assertAll(repo.entries.find().commit(commitIds[1]),
				"ACTOR",
				"FLOW",
				"SOURCE");
		assertAll(repo.entries.find().commit(commitIds[2]),
				"ACTOR",
				"FLOW",
				"SOURCE");
		assertAll(repo.entries.find(),
				"ACTOR",
				"FLOW",
				"SOURCE");
	}

	@Test
	public void testIterateRecursiveWithLibraries() throws IOException {
		var commitIds = new String[] {
				repo.commit(Arrays.asList(Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json"))),
						"library_a"),
				repo.commit(Arrays.asList(), 
						"library_b") };
		assertAll(repo.entries.find().recursive().commit(commitIds[0]),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME,
				RepositoryInfo.FILE_NAME + "/library_a");
		assertAll(repo.entries.find().recursive().commit(commitIds[1]),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME,
				RepositoryInfo.FILE_NAME + "/library_a",
				RepositoryInfo.FILE_NAME + "/library_b");
	}

	@Test
	public void testIteratePath() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(repo.entries.find().commit(commitIds[0]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().commit(commitIds[1]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertAll(repo.entries.find().commit(commitIds[2]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertAll(repo.entries.find().path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");

		assertAll(repo.entries.find().commit(commitIds[0]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().commit(commitIds[1]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().commit(commitIds[2]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
	}

	@Test
	public void testIterateRecursive() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(repo.entries.find().commit(commitIds[0]).recursive(),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"ACTOR/category",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW",
				"FLOW/cat",
				"FLOW/cat/sub",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category:one",
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().commit(commitIds[1]).recursive(),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"ACTOR/category",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW",
				"FLOW/cat",
				"FLOW/cat/sub",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category:one/a.json",
				"SOURCE/category_two",
				"SOURCE/category_zhree",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().commit(commitIds[2]).recursive(),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json",
				"ACTOR/category",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"FLOW",
				"FLOW/cat",
				"FLOW/cat/sub",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category:one/a.json",
				"SOURCE/category_two",
				"SOURCE/category_zhree",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().recursive(),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json",
				"ACTOR/category",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"FLOW",
				"FLOW/cat",
				"FLOW/cat/sub",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category:one/a.json",
				"SOURCE/category_two",
				"SOURCE/category_zhree",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	@Test
	public void testIteratePathRecursive() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(repo.entries.find().commit(commitIds[0]).path("SOURCE/category:one").recursive(),
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().commit(commitIds[1]).path("SOURCE/category:one").recursive(),
				"SOURCE/category:one/a.json");
		assertAll(repo.entries.find().commit(commitIds[2]).path("SOURCE/category:one").recursive(),
				"SOURCE/category:one/a.json");
		assertAll(repo.entries.find().path("SOURCE/category:one").recursive(),
				"SOURCE/category:one/a.json");
	}

	private void assertAll(Find find, String... expected) {
		var refs = find.all();
		Assert.assertEquals(expected.length, refs.size());
		for (var i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], refs.get(i).path);
		}
	}

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}

}
