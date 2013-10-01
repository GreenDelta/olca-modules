package org.openlca.simapro.csv.parser;

import org.openlca.simapro.csv.model.SPQuantity;

final class Quantity {

	static SPQuantity parse(String line, String csvSeperator) {
		String name = line.substring(0, line.indexOf(csvSeperator));
		line = line.substring(0, line.indexOf(csvSeperator));
		String dimensional = line;
		if (dimensional.contains(csvSeperator)) {
			dimensional = dimensional.substring(0,
					dimensional.indexOf(csvSeperator));
		}
		SPQuantity quantity = new SPQuantity(name, null);
		quantity.setDimensional(dimensional.equals("Yes"));

		return quantity;
	}
}
