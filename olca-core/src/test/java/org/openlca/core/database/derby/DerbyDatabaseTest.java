package org.openlca.core.database.derby;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.util.Dirs;

public class DerbyDatabaseTest {

	@Test
	public void testDumpMemoryDB() throws Exception {
		DerbyDatabase db = DerbyDatabase.createInMemory();
		String name = db.getName();
		Actor a = new Actor();
		a.setName("The Donald");
		a = new ActorDao(db).insert(a);
		long id = a.getId();
		Path path = Files.createTempDirectory("_olca_test_");
		db.dump(path.toString());
		db.close();
		db = DerbyDatabase.restoreInMemory(
				path.resolve("./" + name).toString());
		a = new ActorDao(db).getForId(id);
		assertEquals("The Donald", a.getName());
		db.close();
		Dirs.delete(path);
	}
}
