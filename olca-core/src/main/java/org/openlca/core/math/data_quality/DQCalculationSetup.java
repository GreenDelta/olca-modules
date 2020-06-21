package org.openlca.core.math.data_quality;

import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ProductSystem;

public class DQCalculationSetup {

	// TODO: => does not work with in memory
	// product systems
	public long productSystemId;

	public AggregationType aggregationType;

	/**
	 * Indicates whether values should be rounded in `ceiling` mode (e.g.
	 * 2.1 is rounded to 3 in this case). If this field is set to false, normal
	 * `half-up` rounding is used.
	 */
	public boolean ceiling;

	public NAHandling naHandling;
	public DQSystem processSystem;
	public DQSystem exchangeSystem;

	/**
	 * Initializes the setup with default settings. The data
	 * quality systems are initialized with the respective
	 * values from the reference process of the system (which
	 * may be null).
	 */
	public static DQCalculationSetup of(ProductSystem system) {
		var setup = new DQCalculationSetup();
		setup.aggregationType = AggregationType.WEIGHTED_AVERAGE;
		setup.ceiling = false;
		setup.naHandling = NAHandling.EXCLUDE;
		if (system == null)
			return setup;
		setup.productSystemId = system.id;
		if (system.referenceProcess == null)
			return setup;
		var ref = system.referenceProcess;
		setup.exchangeSystem = ref.exchangeDqSystem;
		setup.processSystem = ref.dqSystem;
		return setup;
	}

}
