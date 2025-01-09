package org.openlca.io.smartepd;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.util.Strings;

public class MethodMappingTest {

	@Test
	public void testGetAll() {
		var mappings = MethodMapping.getAll();
		assertFalse(mappings.isEmpty());
		for (var m : mappings) {

			// check that it is a valid SmartEPD method
			var method = SmartMethod.of(m.smartEpd()).orElse(null);
			assertNotNull(method);

			// check that the indicator mappings are valid
			assertFalse(m.indicators().isEmpty());
			for (var i : m.indicators()) {

				if (Strings.notEmpty(i.smartEpd())) {
					var indicator = SmartIndicator.of(i.smartEpd()).orElse(null);
					assertNotNull("unknown indicator: " + i.smartEpd(), indicator);
					if (!indicator.id.equals(i.smartEpd())) {
						System.out.println("fix case: " + indicator.id + " <> " + i.smartEpd());
					}
				}

				assertNotNull(i.ref());
				assertFalse(Strings.nullOrEmpty(i.ref().id()));
			}
		}
	}

}
