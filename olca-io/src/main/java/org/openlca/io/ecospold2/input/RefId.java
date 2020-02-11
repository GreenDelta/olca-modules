package org.openlca.io.ecospold2.input;

import org.openlca.util.KeyGen;

import spold2.Activity;
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
	 * We generate a UUID for the resulting process in openLCA from the activity
	 * ID and the ID of the reference flow (the product output with an amount >
	 * 0).
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

		return KeyGen.get(activity.id, productID);
	}

	/**
	 * Generates the ID of the provider process (= the linked activity) for the
	 * given product input.
	 */
	public static String linkID(IntermediateExchange input) {
		if (input == null)
			return KeyGen.NULL_UUID;
		String activityID = input.activityLinkId;
		String productID = input.flowId;
		return KeyGen.get(activityID, productID);
	}

}
