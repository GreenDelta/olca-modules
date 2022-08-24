package org.openlca.core.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.model.descriptors.Descriptor;

public class DescriptorsTest {

	@Test
	public void testToDescriptor() throws Exception {
		for (ModelType t : ModelType.values()) {
			var e = t.modelClass.getDeclaredConstructor().newInstance();
			e.name = t.name();
			e.refId = t.name();
			Descriptor d = Descriptor.of(e);
			assertEquals(t, d.type);
			assertEquals(e.name, d.name);
			assertEquals(e.refId, d.refId);
		}
	}

}
