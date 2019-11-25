package org.openlca.io.ecospold2.input;

import org.openlca.util.KeyGen;

import spold2.Activity;
import spold2.DataSet;
import spold2.IntermediateExchange;
import spold2.Representativeness;
import spold2.Spold2;

/**
 * Generates the reference IDs for EcoSpold 02 entities.
 */
final class RefId {

	private RefId() {
	}

	/**
	 * We generate a UUID for the resulting process in openLCA from the
	 * following components:
	 * 
	 * <ol>
	 * <li>the activity ID
	 * <li>the ID of the reference flow (the product output with an amount > 0)
	 * <li>the ID of the system model
	 * <li>the process type ("U" for unit processes, "S" for LCI results)
	 * </ol>
	 */
	public static String forProcess(DataSet ds) {
		Activity activity = Spold2.getActivity(ds);
		if (activity == null)
			return KeyGen.NULL_UUID;

		// product ID
		IntermediateExchange qRef = Spold2.getReferenceProduct(ds);
		String productID = qRef != null && qRef.flowId != null
				? qRef.flowId
				: KeyGen.NULL_UUID;

		// system model ID
		Representativeness repri = Spold2.getRepresentativeness(ds);
		String systemID = repri != null && repri.systemModelId != null
				? repri.systemModelId
				: KeyGen.NULL_UUID;

		// process type
		String type = activity.type == 2 ? "S" : "U";

		return KeyGen.get(activity.id, productID, systemID, type);
	}

}
