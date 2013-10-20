package org.openlca.io.ecospold2;

import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.IntermediateExchange;
import org.openlca.io.KeyGen;

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
	public static String forProcess(DataSet dataSet) {
		if (dataSet.getActivity() == null)
			return KeyGen.NULL_UUID;
		String productId = null;
		for (IntermediateExchange exchange : dataSet.getIntermediateExchanges()) {
			if (exchange.getOutputGroup() == null)
				continue;
			if (exchange.getOutputGroup() == 0 && exchange.getAmount() != 0) {
				productId = exchange.getIntermediateExchangeId();
				break;
			}
		}
		return KeyGen.get(dataSet.getActivity().getId(), productId);
	}

	/**
	 * Returns the combination of product ID and related activity ID (in this
	 * order) as UUID.
	 */
	public static String forProductFlow(DataSet dataSet,
			IntermediateExchange productFlow) {
		if (dataSet.getActivity() == null || productFlow == null)
			return KeyGen.NULL_UUID;
		if (productFlow.getOutputGroup() != null)
			return KeyGen.get(productFlow.getIntermediateExchangeId(), dataSet
					.getActivity().getId());
		if (productFlow.getActivityLinkId() == null)
			return productFlow.getIntermediateExchangeId();
		else
			return KeyGen.get(productFlow.getIntermediateExchangeId(),
					productFlow.getActivityLinkId());
	}

}
