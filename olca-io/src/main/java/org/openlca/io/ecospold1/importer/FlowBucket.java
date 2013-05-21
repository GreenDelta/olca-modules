package org.openlca.io.ecospold1.importer;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;

/**
 * A simple structure that holds a flow and conversion factor used for the
 * import. The conversion factor is relevant for the mapped flows and should be
 * 1 for the other case.
 */
class FlowBucket {

	Flow flow;
	double conversionFactor;
	FlowPropertyFactor flowProperty;
	Unit unit;

	boolean isValid() {
		return flow != null && flowProperty != null && unit != null;
	}

}
