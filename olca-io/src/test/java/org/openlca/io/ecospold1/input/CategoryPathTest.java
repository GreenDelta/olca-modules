package org.openlca.io.ecospold1.input;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.After;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.io.Tests;

/// Tests how the `category` and `subCategory` attributes of an EcoSpold 1
/// data set are split into a path of category names and how this path is
/// used in the import: for creating the category hierarchy and for finding
/// existing flows.
public class CategoryPathTest {

	@After
	public void cleanup() {
		Tests.getDb().clear();
	}

	@Test
	public void testSplit() {
		var path = Util.categoryPathOf("A\\D// C\\\\F", " E/G//H\\I");
		assertArrayEquals(
			new String[]{"A", "D", "C", "F", "E", "G", "H", "I"},
			path);
	}

	@Test
	public void testSync() {
		var db = Tests.getDb();
		var path = Util.categoryPathOf("A\\D// C\\\\F", " E/G//H\\I");
		var category = CategoryDao.sync(db, ModelType.FLOW, path);
		// I -> H -> G -> E -> F -> C -> D -> A
		for (var name : new String[]{"I", "H", "G", "E", "F", "C", "D", "A"}) {
			assertNotNull("missing category: " + name, category);
			assertEquals(name, category.name);
			category = category.category;
		}
		assertNull(category);
	}

	@Test
	public void testFindFlowWithSplitCategory() throws Exception {
		var db = Tests.getDb();
		var file = copyDataSet();
		importDataSet(db, file);

		var flow = db.getAll(Flow.class).stream()
			.filter(f -> "wood chips".equals(f.name))
			.findFirst()
			.orElseThrow();
		assertEquals("materials/handling/wood/products",
			flow.category.toPath());

		// simulate a flow from another source: it has a different
		// reference ID and a location but the same category path; the
		// process is deleted so that the data set is imported again
		flow.refId = "some-other-ref-id";
		flow.location = db.insert(Location.of("DE", "DE"));
		db.update(flow);
		db.delete(db.getAll(Process.class).getFirst());

		// the flow can then only be found through its category path
		// and must not be created a second time
		importDataSet(db, file);
		var count = db.getAll(Flow.class).stream()
			.filter(f -> "wood chips".equals(f.name))
			.count();
		assertEquals(1, count);

		Files.deleteIfExists(file.toPath());
	}

	private File copyDataSet() throws Exception {
		var stream = getClass().getResourceAsStream(
			"/org/openlca/io/ecospold1/category-test.xml");
		assertNotNull(stream);
		var file = Files.createTempFile("olca-", ".xml");
		try (stream) {
			Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
		}
		return file.toFile();
	}

	private void importDataSet(IDatabase db, File file) {
		var imp = new EcoSpold1Import(new ImportConfig(db));
		imp.setFiles(new File[]{file});
		imp.run();
	}
}
