package org.openlca.git.repo;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.repo.Entries.Find;

public class EntriesTests {

	private static TmpConfig config;
	private static ClientRepository repo;
	private static String[] commitIds;

	@BeforeClass
	public static void createRepo() throws IOException {
		config = TmpConfig.create();
		repo = config.repo();
		commitIds = new String[] {
				RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_1),
				RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_2),
				RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_3)
		};
	}

	@Test
	public void testCountRecursive() {
		Assert.assertEquals(19, count(commitIds[0]));
		Assert.assertEquals(17, count(commitIds[1]));
		Assert.assertEquals(21, count(commitIds[2]));
		Assert.assertEquals(21, repo.entries.find().recursive().count());
	}

	private long count(String commitId) {
		return repo.entries.find().recursive().commit(commitId).count();
	}

	@Test
	public void testIterate() {
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
	public void testIteratePath() {
		assertAll(repo.entries.find().commit(commitIds[0]).path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().commit(commitIds[1]).path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().commit(commitIds[2]).path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");

		assertAll(repo.entries.find().commit(commitIds[0]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category_one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().commit(commitIds[1]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().commit(commitIds[2]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
	}

	@Test
	public void testIterateRecursive() {
		assertAll(repo.entries.find().commit(commitIds[0]).recursive(),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
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
				"SOURCE/category_one",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree");
		assertAll(repo.entries.find().commit(commitIds[1]).recursive(),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW",
				"FLOW/cat",
				"FLOW/cat/sub",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
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
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW",
				"FLOW/cat",
				"FLOW/cat/sub",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
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
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW",
				"FLOW/cat",
				"FLOW/cat/sub",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two",
				"SOURCE/category_zhree",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	@Test
	public void testIteratePathRecursive() {
		assertAll(repo.entries.find().commit(commitIds[0]).path("SOURCE/category_one").recursive(),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().commit(commitIds[1]).path("SOURCE/category_one").recursive(),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().commit(commitIds[2]).path("SOURCE/category_one").recursive(),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.entries.find().path("SOURCE/category_one").recursive(),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	private void assertAll(Find find, String... expected) {
		var refs = find.all();
		Assert.assertEquals(expected.length, refs.size());
		for (var i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], refs.get(i).path);
		}
	}

	@AfterClass
	public static void closeRepo() {
		config.close();
	}

}
