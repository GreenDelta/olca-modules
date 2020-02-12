package org.openlca.io.simapro.csv;

import org.openlca.core.model.Category;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.SubCompartment;

class Compartment {
	ElementaryFlowType type;
	SubCompartment sub;

	static Compartment of(ElementaryFlowType type, SubCompartment sub) {
		Compartment c = new Compartment();
		c.type = type;
		c.sub = sub;
		return c;
	}

	static Compartment of(Category category) {
		if (category == null)
			return null;

		// build the path
		StringBuilder p = null;
		Category c = category;
		while (c != null) {
			String name = c.name == null ? "" : c.name;
			if (p == null) {
				p = new StringBuilder(name);
			} else {
				p.insert(0, name + '/');
			}
			c = c.category;
		}
		String path = p.toString().toLowerCase();

		// find compartments for the path
		// try to match more specific first

		// resources
		if (match(path, "resource")) {
			ElementaryFlowType type = ElementaryFlowType.RESOURCES;
			if (match(path, "bio")) {
				return Compartment.of(type,
						SubCompartment.RAW_MATERIAL_BIOTIC);
			}

			if (match(path, "water")) {
				return Compartment.of(type,
						SubCompartment.RAW_MATERIAL_IN_WATER);
			}

			if (match(path, "air")) {
				return Compartment.of(type,
						SubCompartment.RAW_MATERIAL_IN_AIR);
			}

			if (match(path, "land")) {
				return Compartment.of(type,
						SubCompartment.RAW_MATERIAL_LAND);
			}

			if (match(path, "ground")) {
				return Compartment.of(type,
						SubCompartment.RAW_MATERIAL_IN_GROUND);
			}
		}

		return null;
	}

	private static boolean match(String path, String... parts) {
		for (String part : parts) {
			if (!path.contains(part))
				return false;
		}
		return true;
	}

}
