package org.openlca.git.repo;

import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;
import static org.openlca.git.repo.ExampleData.COMMIT_3;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Diff;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.References.Find;
import org.openlca.git.util.BinaryResolver;

public class ReferencesTests extends AbstractRepositoryTests {

	@Test
	public void testCount() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		Assert.assertEquals(8, repo.references.find().commit(commitIds[0]).count());
		Assert.assertEquals(7, repo.references.find().commit(commitIds[1]).count());
		Assert.assertEquals(11, repo.references.find().commit(commitIds[2]).count());
		Assert.assertEquals(11, repo.references.find().count());
	}

	@Test
	public void testFirst() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
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
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
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
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		var firstPath = "SOURCE/category:one/a.json";
		var parentPath = "SOURCE/category:one";
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
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertIterate(repo.references.find().commit(commitIds[0]),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[1]),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[2]),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find(),
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/cAA39f5b-5021_bin1.json+39f082dfae0..json",
				"FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	@Test
	public void testIterateType() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertIterate(repo.references.find().commit(commitIds[0]).type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[1]).type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[2]).type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().type(ModelType.SOURCE),
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/a.json",
				"SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}

	@Test
	public void testIteratePath() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertIterate(repo.references.find().commit(commitIds[0]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertIterate(repo.references.find().commit(commitIds[1]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertIterate(repo.references.find().commit(commitIds[2]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertIterate(repo.references.find().path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
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
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		var refId = "cAA39f5b-5021_bin1.json+39f082dfae0.";
		var ref = repo.references.get(ModelType.ACTOR, refId, commitIds[0]);
		var filenames = repo.references.getBinaries(ref);
		Assert.assertEquals(1, filenames.size());
		Assert.assertEquals("te?st.txt", filenames.get(0));
	}

	@Test
	public void testIncludeCategoriesCountRecursive() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		Assert.assertEquals(19, repo.references.find().includeCategories().commit(commitIds[0]).count());
		Assert.assertEquals(17, repo.references.find().includeCategories().commit(commitIds[1]).count());
		Assert.assertEquals(21, repo.references.find().includeCategories().commit(commitIds[2]).count());
		Assert.assertEquals(21, repo.references.find().includeCategories().count());
	}

	@Test
	public void testIncludeCategoriesIterate() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(repo.references.find().includeCategories().nonRecursive().commit(commitIds[0]),
				"ACTOR",
				"FLOW",
				"SOURCE");
		assertAll(repo.references.find().includeCategories().nonRecursive().commit(commitIds[1]),
				"ACTOR",
				"FLOW",
				"SOURCE");
		assertAll(repo.references.find().includeCategories().nonRecursive().commit(commitIds[2]),
				"ACTOR",
				"FLOW",
				"SOURCE");
		assertAll(repo.references.find().includeCategories().nonRecursive(),
				"ACTOR",
				"FLOW",
				"SOURCE");
	}

	@Test
	public void testIncludeCategoriesIterateRecursiveWithLibraries() throws IOException {
		var commitIds = new String[] {
				repo.commit(Arrays.asList(Diff.added(new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json"))),
						"library_a"),
				repo.commit(Arrays.asList(),
						"library_b") };
		assertAll(repo.references.find().includeCategories().commit(commitIds[0]),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME,
				RepositoryInfo.FILE_NAME + "/library_a");
		assertAll(repo.references.find().includeCategories().commit(commitIds[1]),
				"ACTOR",
				"ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				RepositoryInfo.FILE_NAME,
				RepositoryInfo.FILE_NAME + "/library_a",
				RepositoryInfo.FILE_NAME + "/library_b");
	}

	@Test
	public void testIncludeCategoriesIteratePath() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(
				repo.references.find().includeCategories().nonRecursive().commit(commitIds[0])
						.path("SOURCE/category:one"),
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(
				repo.references.find().includeCategories().nonRecursive().commit(commitIds[1])
						.path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertAll(
				repo.references.find().includeCategories().nonRecursive().commit(commitIds[2])
						.path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertAll(repo.references.find().includeCategories().nonRecursive().path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");

		assertAll(repo.references.find().includeCategories().nonRecursive().commit(commitIds[0]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.references.find().includeCategories().nonRecursive().commit(commitIds[1]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.references.find().includeCategories().nonRecursive().commit(commitIds[2]).path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
		assertAll(repo.references.find().includeCategories().nonRecursive().path("SOURCE"),
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one",
				"SOURCE/category_two",
				"SOURCE/category_zhree");
	}

	@Test
	public void testIncludeCategoriesIterateRecursive() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(repo.references.find().includeCategories().commit(commitIds[0]),
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
		assertAll(repo.references.find().includeCategories().commit(commitIds[1]),
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
		assertAll(repo.references.find().includeCategories().commit(commitIds[2]),
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
		assertAll(repo.references.find().includeCategories(),
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
	public void testIncludeCategoriesIteratePathRecursive() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		assertAll(repo.references.find().includeCategories().commit(commitIds[0]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		assertAll(repo.references.find().includeCategories().commit(commitIds[1]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertAll(repo.references.find().includeCategories().commit(commitIds[2]).path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
		assertAll(repo.references.find().includeCategories().path("SOURCE/category:one"),
				"SOURCE/category:one/a.json");
	}

	private void assertAll(Find find, String... expected) {
		var index = new AtomicInteger();
		find.iterate(ref -> {
			Assert.assertEquals(expected[index.getAndIncrement()], ref.path);
		});
	}

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}
}
