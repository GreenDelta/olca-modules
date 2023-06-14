package org.openlca.core.database.descriptors;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;

public class DescriptorReaderTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testGetAll() throws Exception {
		for (var type : ModelType.values()) {
			var e = type.getModelClass()
					.getDeclaredConstructor()
					.newInstance();
			e.name = "some";
			db.insert(e);
			var d = Descriptor.of(e);
			var ds = DescriptorReader.of(db, type);
			assertTrue(ds.getAll().contains(d));
			assertTrue(ds.getAll(
					r -> e.name.equals(ds.getName(r))).contains(d));
		}
	}

}
