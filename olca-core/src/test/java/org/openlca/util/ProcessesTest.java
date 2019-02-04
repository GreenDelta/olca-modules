package org.openlca.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.function.Supplier;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessesTest {

	@Test
	public void testFindForLabel() {
		IDatabase db = Tests.getDb();
		Supplier<ProcessDescriptor> query = () -> Processes.findForLabel(db,
				"cow milking - CH");
		assertNull(query.get());

		ProcessDao dao = new ProcessDao(db);
		Process p1 = new Process();
		p1.name = "cow milking";
		dao.insert(p1);
		assertEquals(p1.id, query.get().id);

		Location loc = new Location();
		loc.code = "CH";
		Daos.base(db, Location.class).insert(loc);
		Process p2 = new Process();
		p2.name = "cow milking";
		p2.location = loc;
		dao.insert(p2);
		assertEquals(p2.id, query.get().id);

		Daos.base(db, Location.class).delete(loc);
		for (Process p : dao.getForName("cow milking")) {
			dao.delete(p);
		}
		assertNull(query.get());
	}

}
