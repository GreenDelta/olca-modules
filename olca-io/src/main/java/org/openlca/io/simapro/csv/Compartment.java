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

	static Compartment of(String path) {
		if (path == null)
			return null;
		String[] parts = path.split("/");
		int n = parts.length;
		if (parts.length > 1) {
			String sub = parts[n - 1];
			String comp = parts[n - 2];
			Compartment c = find(comp.trim(), sub.trim());
			if (c != null)
				return c;
		}
		return matchPath(path);
	}

	static Compartment of(Category category) {
		if (category == null)
			return null;

		// try to directly identify the compartment
		// from the last two category names
		if (category.category != null) {
			String comp = category.category.name;
			String sub = category.name;
			if (comp != null && sub != null) {
				Compartment c = find(comp.trim(), sub.trim());
				if (c != null)
					return c;
			}
		}

		// try to identify it from identifiers in the
		// full category path
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
		return matchPath(path);
	}

	private static Compartment matchPath(String path) {
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

			return Compartment.of(type, SubCompartment.UNSPECIFIED);
		}

		// air emissions
		if (match(path, "emission", "air")) {
			ElementaryFlowType type = ElementaryFlowType.EMISSIONS_TO_AIR;

			if (match(path, "stratosphere", "troposhere")) {
				return Compartment.of(type,
						SubCompartment.AIRBORNE_STATOSPHERE_TROPOSHERE);
			}

			if (match(path, "stratosphere")) {
				return Compartment.of(type,
						SubCompartment.AIRBORNE_STATOSPHERE);
			}

			if (match(path, "low", "pop", "long", "term")) {
				return Compartment.of(type,
						SubCompartment.AIRBORNE_LOW_POP_LONG_TERM);
			}

			if (match(path, "low", "pop")) {
				return Compartment.of(type,
						SubCompartment.AIRBORNE_LOW_POP);
			}

			if (match(path, "high", "pop")) {
				return Compartment.of(type,
						SubCompartment.AIRBORNE_HIGH_POP);
			}

			if (match(path, "indoor")) {
				return Compartment.of(type,
						SubCompartment.AIRBORNE_INDOOR);
			}

			return Compartment.of(type, SubCompartment.UNSPECIFIED);
		}

		// soil emissions
		if (match(path, "emission", "soil")) {
			ElementaryFlowType type = ElementaryFlowType.EMISSIONS_TO_SOIL;

			if (match(path, "non-agri")) {
				return Compartment.of(type,
						SubCompartment.UNSPECIFIED);
			}

			if (match(path, "agri")) {
				return Compartment.of(type,
						SubCompartment.SOIL_AGRICULTURAL);
			}

			if (match(path, "forest")) {
				return Compartment.of(type,
						SubCompartment.SOIL_FORESTRY);
			}

			if (match(path, "urban")) {
				return Compartment.of(type,
						SubCompartment.SOIL_URBAN);
			}

			if (match(path, "industrial")) {
				return Compartment.of(type,
						SubCompartment.SOIL_INDUSTRIAL);
			}

			return Compartment.of(type, SubCompartment.UNSPECIFIED);
		}

		// water emissions
		if (match(path, "emission", "water")) {
			ElementaryFlowType type = ElementaryFlowType.EMISSIONS_TO_WATER;

			if (match(path, "fossil")) {
				return Compartment.of(type,
						SubCompartment.WATERBORNE_FOSSILWATER);
			}

			if (match(path, "ground", "long", "term")) {
				return Compartment.of(type,
						SubCompartment.WATERBORNE_GROUNDWATER_LONG_TERM);
			}

			if (match(path, "ground")) {
				return Compartment.of(type,
						SubCompartment.WATERBORNE_GROUNDWATER);
			}

			if (match(path, "lake")) {
				return Compartment.of(type,
						SubCompartment.WATERBORNE_LAKE);
			}

			if (match(path, "ocean")) {
				return Compartment.of(type,
						SubCompartment.WATERBORNE_LAKE);
			}

			if (match(path, "river")) {
				return Compartment.of(type,
						SubCompartment.WATERBORNE_RIVER);
			}

			if (match(path, "river", "long", "term")) {
				return Compartment.of(type,
						SubCompartment.WATERBORNE_RIVER_LONG_TERM);
			}

			return Compartment.of(type, SubCompartment.UNSPECIFIED);
		}

		if (match(path, "land")) {
			return Compartment.of(ElementaryFlowType.RESOURCES,
					SubCompartment.RAW_MATERIAL_LAND);
		}

		return null;
	}

	/**
	 * Try to find the compartment pair directly from the given compartment and
	 * sub-compartment name.
	 */
	private static Compartment find(String compartment, String subCompartment) {
		ElementaryFlowType type = null;
		for (ElementaryFlowType t : ElementaryFlowType.values()) {
			if (t.getReferenceHeader().equalsIgnoreCase(compartment)
					|| t.getExchangeHeader().equalsIgnoreCase(compartment)) {
				type = t;
				break;
			}
		}
		if (type == null)
			return null;

		SubCompartment sub = null;
		for (SubCompartment s : SubCompartment.values()) {
			if (s.getValue().equalsIgnoreCase(subCompartment)) {
				sub = s;
				break;
			}
		}
		return sub == null ? null : Compartment.of(type, sub);
	}

	private static boolean match(String path, String... parts) {
		for (String part : parts) {
			if (!path.contains(part))
				return false;
		}
		return true;
	}

}
