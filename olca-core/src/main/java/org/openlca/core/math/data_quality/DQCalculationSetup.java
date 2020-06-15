package org.openlca.core.math.data_quality;

import java.math.RoundingMode;

import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ProductSystem;

public class DQCalculationSetup {

	// TODO: => does not work with in memory
	// product systems
	public long productSystemId;

	public AggregationType aggregationType;
	public RoundingMode roundingMode;
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
		setup.roundingMode = RoundingMode.HALF_UP;
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
