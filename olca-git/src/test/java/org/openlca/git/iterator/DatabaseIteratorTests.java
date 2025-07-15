package org.openlca.git.iterator;

import java.io.IOException;

import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.RepositoryInfo;

public class DatabaseIteratorTests extends AbstractRepositoryTests {

	@Test
	public void testDatabaseIteration() throws IOException {
		repo.create(
				"ACTOR/0.json",
				"ACTOR/caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json",
				"FLOW/cat/sub/DCA39f5b_5021.4bb-9330-739f082dfae0..json",
				"SOURCE/a_category",
				"SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/c_category",
				"SOURCE/category:one/aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"SOURCE/category_zhree");
		repo.assertEqual(new DatabaseIterator(repo), 
				"ACTOR", 
				"FLOW", 
				"SOURCE",
				RepositoryInfo.FILE_NAME);
		repo.assertEqual(new DatabaseIterator(repo, "ACTOR"),
				"0.json",
				"caa39f5b-5021-4b6b-9330-739f082dfae0.json",
				"category");
		repo.assertEqual(new DatabaseIterator(repo, "ACTOR/category"),
				"0ba39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqual(new DatabaseIterator(repo, "FLOW"), 
				"cat");
		repo.assertEqual(new DatabaseIterator(repo, "FLOW/cat"),
				"sub");
		repo.assertEqual(new DatabaseIterator(repo, "FLOW/cat/sub"),
				"DCA39f5b_5021.4bb-9330-739f082dfae0..json");
		repo.assertEqual(new DatabaseIterator(repo, "SOURCE"),
				"a_category",
				"bca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"c_category",
				"category:one",
				"category_two",
				"category_zhree");
		repo.assertEqual(new DatabaseIterator(repo, "SOURCE/category:one"),
				"aca39f5b-5021-4b6b-9330-739f082dfae0.json",
				"aca49f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqual(new DatabaseIterator(repo, "SOURCE/category_two"),
				"0ca39f5b-5021-4b6b-9330-739f082dfae0.json");
	}
	
	@Test
	public void testMove() throws IOException {
		repo.create("ACTOR/category/0.json",
				"ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.commitWorkspace();
		repo.move("ACTOR/category/0.json", "category2");
		repo.move("ACTOR/category2/1aa39f5b-5021-4b6b-9330-739f082dfae0.json", "category");
		repo.assertEqual(new DatabaseIterator(repo), 
				"ACTOR",
				RepositoryInfo.FILE_NAME);
		repo.assertEqual(new DatabaseIterator(repo, "ACTOR"), 
				"category", 
				"category2");
		repo.assertEqual(new DatabaseIterator(repo, "ACTOR/category"), 
				"1aa39f5b-5021-4b6b-9330-739f082dfae0.json");
		repo.assertEqual(new DatabaseIterator(repo, "ACTOR/category2"), 
				"0.json");
	}
	
}
