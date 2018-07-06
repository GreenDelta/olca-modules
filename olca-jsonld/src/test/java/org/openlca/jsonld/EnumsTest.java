package org.openlca.jsonld;

import static org.junit.Assert.assertEquals;

import java.nio.file.StandardCopyOption;

import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;

public class EnumsTest {

	@Test
	public void testGetUnmappedEnum() {
		StandardCopyOption opt = Enums.getValue("ATOMIC_MOVE",
			StandardCopyOption.class);
		assertEquals(StandardCopyOption.ATOMIC_MOVE, opt);
		assertEquals("ATOMIC_MOVE", Enums.getLabel(opt));
	}

	@Test
	public void testModelType() {
		ModelType type = Enums.getValue("FLOW", ModelType.class);
		assertEquals(ModelType.FLOW, type);
		assertEquals("FLOW", Enums.getLabel(type));
	}

	@Test
	public void testParameterScope() {
		ParameterScope scope = Enums.getValue("GLOBAL_SCOPE",
			ParameterScope.class);
		assertEquals(ParameterScope.GLOBAL, scope);
		assertEquals("GLOBAL_SCOPE", Enums.getLabel(scope));
	}

}
