package org.openlca.git.iterator;

import java.io.IOException;

import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.TreeValidator;

public class DatabaseIteratorTests extends AbstractRepositoryTests {

	@Test
	public void testDatabaseIteration() throws IOException {
		create(
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
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo), "ACTOR", "FLOW", "SOURCE");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "ACTOR"),
				"0aa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"category");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "ACTOR/category"),
				"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "FLOW"), "cat");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "FLOW/cat"), "sub");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "FLOW/cat/sub"),
				"dca39f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "SOURCE"),
				"a_category",
				"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"c_category",
				"category_one",
				"category_two",
				"category_zhree");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "SOURCE/category_one"),
				"aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		TreeValidator.assertEqual(repo, new DatabaseIterator(repo, "SOURCE/category_two"),
				"0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}
}
