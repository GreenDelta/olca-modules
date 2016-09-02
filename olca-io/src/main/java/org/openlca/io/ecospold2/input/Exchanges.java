package org.openlca.io.ecospold2.input;

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
		for (IntermediateExchange e : ds.getIntermediateExchanges()) {
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

}
