package org.openlca.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;

public class CategoriesTest {

	@Test
	public void testFindOrAdd() {
		IDatabase db = Tests.getDb();
		String[] path = { "A", "B", "C", "D", "E" };
		Category c = null;
		for (int i = 0; i < 10; i++) {
			Category next = Categories.findOrAdd(db, ModelType.FLOW, path);
			if (c != null) {
				assertEquals(c.id, next.id);
			} else {
				c = next;
			}
		}
		checkCat(path, c);
	}

	@Test
	public void testFindOrAddModel() {
		IDatabase db = Tests.getDb();
		String[] path = { "A", "B", "C", "D", "E" };
		Category c = null;
		for (int i = 0; i < 10; i++) {
			Category next = Categories.findOrAdd(db, ModelType.FLOW, path);
			if (c != null) {
				assertEquals(c.id, next.id);
			} else {
				c = next;
			}
			Flow f = new Flow();
			f.category = c;
			FlowDao dao = new FlowDao(db);
			dao.insert(f);
			f = dao.getForId(f.id);
			checkCat(path, f.category);
		}
	}

	private void checkCat(String[] path, Category cat) {
		Category c = cat;
		for (int i = path.length - 1; i >= 0; i--) {
			assertEquals(c.name, path[i]);
			c = c.category;
		}
	}

}
