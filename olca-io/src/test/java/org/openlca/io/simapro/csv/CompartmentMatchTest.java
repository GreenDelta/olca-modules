package org.openlca.io.simapro.csv;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;

public class CompartmentMatchTest {


	@Test
	public void testMatching() {


		check("Emissions to air/high population density",
			ElementaryFlowType.EMISSIONS_TO_AIR,
			SubCompartment.AIR_HIGH_POP);


	}

	private void check(String path, ElementaryFlowType type, SubCompartment sub) {

		var comp = Compartment.fromPath(path);
		assertNotNull("No compartment found for: " + path, comp);
		assertEquals("Unexpected type for: " + path, type, comp.type());
		assertEquals("Unexpected sub-compartment for: " + path,
			SubCompartment.AIR_HIGH_POP, comp.sub());

		Category category = null;
		for (var seg : path.split("/")) {
			var c = Category.of(seg.strip(), ModelType.FLOW);
			if (category != null) {
				category.childCategories.add(c);
				c.category = category;
			}
			category = c;
		}

		comp = Compartment.of(category);
		assertNotNull("No compartment found for: " + path, comp);
		assertEquals("Unexpected type for: " + path, type, comp.type());
		assertEquals("Unexpected sub-compartment for: " + path,
			SubCompartment.AIR_HIGH_POP, comp.sub());
	}

}
