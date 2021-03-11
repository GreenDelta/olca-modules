package org.openlca.core.database.derby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.database.ActorDao;
import org.openlca.core.database.Derby;
import org.openlca.core.model.Actor;
import org.openlca.util.Dirs;

public class DerbyDatabaseTest {

	@Test
	public void testDumpMemoryDB() throws Exception {
		Derby db = Derby.createInMemory();
		Actor a = new Actor();
		a.name = "The Donald";
		a = new ActorDao(db).insert(a);
		long id = a.id;
		Path path = Files.createTempDirectory("_olca_test_");
		db.dump(path.toString());
		db.close();
		db = Derby.restoreInMemory(path.toString());
		a = new ActorDao(db).getForId(id);
		assertEquals("The Donald", a.name);
		db.close();
		Dirs.delete(path);
	}

	@Test
	@Ignore
	public void testNoMemLeak() throws Exception {
		Runtime rt = Runtime.getRuntime();
		long initialUsed = rt.totalMemory() - rt.freeMemory();
		for (int i = 0; i < 1000; i++) {
			Derby db = Derby.createInMemory();
			db.close();
			long usedMem = rt.totalMemory() - rt.freeMemory();
			if (initialUsed * 10 < usedMem) {
				fail("There is probably a memory leak");
			}
			System.out.println("" + i + "\t" + (usedMem / (1024 * 1024)));
		}
	}

	@Test
	@Ignore
	public void testFromFolder() throws Exception {
		File dir = Files.createTempDirectory("olca_test_db").toFile();
		assertTrue(dir.delete());
		try (Derby db = new Derby(dir)) {
			assertEquals(dir.getName(), db.getName());
		}
		Dirs.delete(dir.getAbsolutePath());
	}
}
