package org.openlca.io.ecospold2.input;

import org.openlca.util.KeyGen;

import spold2.DataSet;
import spold2.IntermediateExchange;
import spold2.Spold2;

/**
 * Generates the reference IDs for EcoSpold 02 entities.
 */
final class RefId {

	private RefId() {
	}

	/**
	 * Returns the combination of activity-ID and reference product-ID (in this
	 * order) as UUID.
	 */
	public static String forProcess(DataSet ds) {
		if (Spold2.getActivity(ds) == null)
			return KeyGen.NULL_UUID;
		String productId = null;
		for (IntermediateExchange exchange : Spold2.getProducts(ds)) {
			if (exchange.outputGroup == null)
				continue;
			if (exchange.outputGroup == 0 && exchange.amount != 0) {
				productId = exchange.flowId;
				break;
			}
		}
		return KeyGen.get(Spold2.getActivity(ds).id, productId);
	}

}
