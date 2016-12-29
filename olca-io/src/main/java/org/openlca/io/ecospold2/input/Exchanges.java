package org.openlca.io.ecospold2.input;

import java.util.Objects;

import spold2.DataSet;
import spold2.IntermediateExchange;
import spold2.Spold2;

class Exchanges {

	private Exchanges() {
	}

	/** Returns true if the given exchanges are equal; i.e. have the same ID. */
	static boolean eq(IntermediateExchange e1, IntermediateExchange e2) {
		if (e1 == null && e2 == null)
			return true;
		if (e1 == null || e2 == null)
			return false;
		return Objects.equals(e1.id, e2.id);
	}

	/**
	 * Returns true if the given product flow is an input that is linked to the
	 * given reference flow of the given data set.
	 */
	static boolean isSelfLoop(IntermediateExchange candidate,
			IntermediateExchange ref, DataSet ds) {
		if (candidate == null || ref == null || ds == null)
			return false;
		if (candidate.inputGroup == null
				|| candidate.amount == null
				|| candidate.amount.doubleValue() == 0
				|| candidate.activityLinkId == null)
			return false;
		return Objects.equals(candidate.flowId,
				ref.flowId)
				&& Objects.equals(candidate.activityLinkId,
						Spold2.getId(ds));
	}

}
