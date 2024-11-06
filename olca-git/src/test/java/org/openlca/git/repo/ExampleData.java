package org.openlca.git.repo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.model.Diff;
import org.openlca.git.model.Reference;

class ExampleData extends AbstractRepositoryTests {

	static final Map<String, List<String>> PATH_TO_BINARY = Map.of(
			"ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json", Arrays.asList("te?st.txt"));

	static final List<Diff> COMMIT_1 = Arrays.asList(
			Diff.added(new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Diff.added(new Reference("ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json")),
			Diff.added(new Reference("ACTOR/category/0ba39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Diff.added(new Reference("FLOW/cat/sub/dca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Diff.added(new Reference("SOURCE/a_category")),
			Diff.added(new Reference("SOURCE/bca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Diff.added(new Reference("SOURCE/c_category")),
			Diff.added(new Reference("SOURCE/category:one/a.json")),
			Diff.added(new Reference("SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
			Diff.added(new Reference("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Diff.added(new Reference("SOURCE/category_zhree")));

	static final List<Diff> COMMIT_2 = Arrays.asList(
			// delete empty category
			Diff.deleted(new Reference("SOURCE/c_category")),
			// delete one of several data sets in a category
			Diff.deleted(new Reference("SOURCE/category:one/aca49f5b-5021-4b6b-9330-739f082dfae0.json")),
			// delete last data set in a category (must create .empty)
			Diff.deleted(new Reference("SOURCE/category_two/0ca39f5b-5021-4b6b-9330-739f082dfae0.json")),
			// add data set in empty category (must delete .empty)
			Diff.added(new Reference("SOURCE/category_zhree/fca39f5b-5021-4b6b-9330-739f082dfae0.json")));

	static final List<Diff> COMMIT_3 = Arrays.asList(
			Diff.modified(new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json"),
					new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae0.json")),
			Diff.added(new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae1.json")),
			Diff.added(new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae2.json")),
			Diff.added(new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae3.json")),
			Diff.added(new Reference("ACTOR/0aa39f5b-5021-4b6b-9330-739f082dfae4.json")),
			Diff.deleted(new Reference("ACTOR/cAA39f5b-5021_bin1.json+39f082dfae0..json")), // move
			Diff.added(new Reference("ACTOR/category/cAA39f5b-5021_bin1.json+39f082dfae0..json")));

}
