package org.openlca.core.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.model.descriptors.Descriptor;

public class DescriptorsTest {

	@Test
	public void testToDescriptor() throws Exception {
		for (ModelType t : ModelType.values()) {
			Class<? extends RootEntity> clazz = t.modelClass;
			if (clazz == null)
				continue;
			RootEntity e = clazz.newInstance();
			e.name = t.name();
			e.refId = t.name();
			Descriptor d = Descriptor.of(e);
			assertEquals(t, d.type);
			assertEquals(e.name, d.name);
			assertEquals(e.refId, d.refId);
		}
	}

}
