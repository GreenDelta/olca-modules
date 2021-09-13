package org.openlca.core.math.data_quality;

import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

public class DQCalculationSetup {

	// TODO: => does not work with in memory
	// product systems
	// public long productSystemId;

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

	public DQCalculationSetup() {
		aggregationType = AggregationType.WEIGHTED_AVERAGE;
		ceiling = false;
		naHandling = NAHandling.EXCLUDE;
	}

	/**
	 * Initializes the setup with default settings. The data
	 * quality systems are initialized with the respective
	 * values from the reference process of the system (which
	 * may be null).
	 */
	public static DQCalculationSetup of(ProductSystem system) {
		return system == null
			? new DQCalculationSetup()
			: of(system.referenceProcess);
	}

	public static DQCalculationSetup of(CalculationSetup setup) {
		return setup == null
			? new DQCalculationSetup()
			: of(setup.process());
	}

	public static DQCalculationSetup of(Process process) {
		var setup = new DQCalculationSetup();
		if (process != null) {
			setup.exchangeSystem = process.exchangeDqSystem;
			setup.processSystem = process.dqSystem;
		}
		return setup;
	}


}
