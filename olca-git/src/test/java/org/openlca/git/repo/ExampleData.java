package org.openlca.git.repo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.model.Change;
import org.openlca.git.model.ModelRef;

class ExampleData extends AbstractRepositoryTests {

	static final Map<String, List<String>> PATH_TO_BINARY = Map.of(
			"ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json", Arrays.asList("test.txt"));

	static final List<Change> COMMIT_1 = Arrays.asList(
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json")),
			Change.add(new ModelRef("ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/a_category")),
			Change.add(new ModelRef("SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/c_category")),
			Change.add(new ModelRef("SOURCE/category_one/a.json")),
			Change.add(new ModelRef("SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("SOURCE/category_zhree")));

	static final List<Change> COMMIT_2 = Arrays.asList(
			// delete empty category
			Change.delete(new ModelRef("SOURCE/c_category")),
			// delete one of several data sets in a category
			Change.delete(new ModelRef("SOURCE/category_one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
			// delete last data set in a category (must create .empty)
			Change.delete(new ModelRef("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			// add data set in empty category (must delete .empty)
			Change.add(new ModelRef("SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json")));

	static final List<Change> COMMIT_3 = Arrays.asList(
			Change.modify(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json")),
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json")),
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json")),
			Change.add(new ModelRef("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json")),
			Change.delete(new ModelRef("ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json")), // move
			Change.add(new ModelRef("ACTOR/category/cAA39f5b-5021_bin1.json+39f082dfae0..json")));

}
