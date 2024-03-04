package org.openlca.git.repo;

import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;
import static org.openlca.git.repo.ExampleData.COMMIT_3;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.References.Find;
import org.openlca.git.util.BinaryResolver;

public class ReferencesTests extends AbstractRepositoryTests {

	@Test
	public void testCount() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		Assert.assertEquals(8, count(commitIds[0]));
		Assert.assertEquals(7, count(commitIds[1]));
		Assert.assertEquals(11, count(commitIds[2]));
		Assert.assertEquals(11, repo.references.find().count());
	}

	private long count(String commitId) {
		return repo.references.find().commit(commitId).count();
	}

	@Test
	public void testFirst() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		var firstModel = "ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json";
		Assert.assertEquals(firstModel, first(commitIds[0]).path);
		Assert.assertEquals(firstModel, first(commitIds[1]).path);
		Assert.assertEquals(firstModel, first(commitIds[2]).path);
		Assert.assertEquals(firstModel, repo.references.find().first().path);
	}

	private Reference first(String commitId) {
		return repo.references.find().commit(commitId).first();
	}

	@Test
	public void testFirstType() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		var firstSource = "SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json";
		Assert.assertEquals(firstSource, first(ModelType.SOURCE, commitIds[0]).path);
		Assert.assertEquals(firstSource, first(ModelType.SOURCE, commitIds[1]).path);
		Assert.assertEquals(firstSource, first(ModelType.SOURCE, commitIds[2]).path);
		Assert.assertEquals(firstSource, repo.references.find().type(ModelType.SOURCE).first().path);
	}

	private Reference first(ModelType type, String commitId) {
		return repo.references.find().type(type).commit(commitId).first();
	}

	@Test
	public void testFirstPath() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		var firstPath = "SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json";
		var parentPath = "SOURCE/category_one";
		Assert.assertEquals(firstPath, first(parentPath, commitIds[0]).path);
		Assert.assertEquals(firstPath, first(parentPath, commitIds[1]).path);
		Assert.assertEquals(firstPath, first(parentPath, commitIds[2]).path);
		Assert.assertEquals(firstPath, repo.references.find().path(parentPath).first().path);
	}

	private Reference first(String path, String commitId) {
		return repo.references.find().path(path).commit(commitId).first();
	}

	@Test
	public void testIterate() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		assertIterate(repo.references.find().commit(commitIds[0]),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[1]),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[2]),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	@Test
	public void testIterateType() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		assertIterate(repo.references.find().commit(commitIds[0]).type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[1]).type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[2]).type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	@Test
	public void testIteratePath() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		assertIterate(repo.references.find().commit(commitIds[0]).path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[1]).path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[2]).path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().path("SOURCE/category_one"),
				"SOURCE/category_one/aca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	private void assertIterate(Find find, String... expected) {
		var count = new AtomicInteger(0);
		find.iterate(ref -> {
			var i = count.getAndIncrement();
			Assert.assertEquals(expected[i], ref.path);
		});
	}

	@Test
	public void testBinaries() throws IOException {
		var commitIds = new String[] { commit(COMMIT_1), commit(COMMIT_2), commit(COMMIT_3) };
		var refId = "caa39f5b-5021-4b6b-9330-739f082dfae0";
		var ref = repo.references.get(ModelType.ACTOR, refId, commitIds[0]);
		var filenames = repo.references.getBinaries(ref);
		Assert.assertEquals(1, filenames.size());
		Assert.assertEquals("test.txt", filenames.get(0));
	}

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}

}
