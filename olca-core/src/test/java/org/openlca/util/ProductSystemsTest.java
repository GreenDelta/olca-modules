package org.openlca.util;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;

public class ProductSystemsTest {

	@Test
	public void testProcessesOf() {
		var db = Tests.getDb();
		var ps1 = new ProductSystem();
		var p1 = db.insert(new Process());
		var p2 = db.insert(new Process());
		var ps2 = new ProductSystem();
		var p3 = db.insert(new Process());
		var p4 = db.insert(new Process());
		ps1.processes.add(p1.id);
		ps1.processes.add(p2.id);
		ps1 = db.insert(ps1);
		ps2.processes.add(p3.id);
		ps2 = db.insert(ps2);
		var processes = ProductSystems.processesOf(db, Descriptor.of(ps1));
		Assert.assertEquals(2, processes.size());
		Assert.assertTrue(processes.contains(p1.id));
		Assert.assertTrue(processes.contains(p2.id));
		Assert.assertTrue(!processes.contains(p3.id));
		Assert.assertTrue(!processes.contains(p4.id));
	}

}
