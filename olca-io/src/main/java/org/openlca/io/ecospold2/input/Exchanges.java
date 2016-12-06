package org.openlca.io.ecospold2.input;

import java.util.Objects;

import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.IntermediateExchange;

class Exchanges {

	private Exchanges() {
	}

	/** Find the reference flow (outputGroup=0) from the given data set. */
	static IntermediateExchange findRef(DataSet ds) {
		if (ds == null)
			return null;
		IntermediateExchange candidate = null;
		for (IntermediateExchange e : In.products(ds)) {
			Integer og = e.outputGroup;
			if (og == null || og.intValue() != 0)
				continue;
			Double a = e.amount;
			if (a != null && a.doubleValue() != 0)
				return e;
			candidate = e;
		}
		return candidate;
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
		return Objects.equals(candidate.intermediateExchangeId,
				ref.intermediateExchangeId)
				&& Objects.equals(candidate.activityLinkId,
						In.id(ds));
	}

}
