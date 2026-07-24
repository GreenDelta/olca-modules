package org.openlca.io.ecospold1;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Stack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.io.Tests;
import org.openlca.io.ecospold1.input.EcoSpold1Import;
import org.openlca.io.ecospold1.input.ImportConfig;

public class CategoryTest {

	private final IDatabase db = Tests.getDb();
	private Process process;

	@Before
	public void setup() throws Exception {
		var stream = getClass().getResourceAsStream("category-test.xml");
		assertNotNull(stream);
		var path = Files.createTempFile("olca-", ".xml");
		try (stream) {
			Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
		}

		var config = new ImportConfig(db);
		var imp = new EcoSpold1Import(config);
		imp.setFiles(new File[] {path.toFile()});
		imp.run();
		Files.delete(path);

		process = db.getForName(Process.class, "compost service");
		assertNotNull(process);
	}

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testCategorySlashes() {
		check(process.category,
			"waste",
			"treatment",
			"community",
			"composting" );

		check(flow("compost service").category,
			"waste",
			"treatment",
			"composting"
		);

		check(flow("wood chips").category,
			"materials",
			"handling",
			"wood",
			"products"
		);
}

	private Flow flow(String name) {
		return process.exchanges
			.stream()
			.filter(e -> e.flow.name.equals(name))
			.map(e -> e.flow)
			.findFirst()
			.orElseThrow();
	}

	private void check(Category category, String... path) {
		var stack = new Stack<String>();
		for (var segment : path) {
			stack.push(segment);
		}
		var cat = category;
		while (!stack.isEmpty()) {
			var expected = stack.pop();
			assertNotNull("segment not found: " + expected , cat);
			assertEquals(expected, cat.name);
			cat = cat.category;
		}
		assertNull("end in hierarchy expected", cat);
	}
}
