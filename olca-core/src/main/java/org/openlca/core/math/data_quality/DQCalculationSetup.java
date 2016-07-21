package org.openlca.core.math.data_quality;

import java.math.RoundingMode;

import org.openlca.core.model.DQSystem;

public class DQCalculationSetup {

	public final long productSystemId;
	public final AggregationType aggregationType;
	public final RoundingMode roundingMode;
	public final ProcessingType processingType;
	public final DQSystem processDqSystem;
	public final DQSystem exchangeDqSystem;

	public DQCalculationSetup(long productSystemId, AggregationType aggregationType, RoundingMode roundingMode,
			ProcessingType processingType, DQSystem processDqSystem, DQSystem exchangeDqSystem) {
		this.productSystemId = productSystemId;
		this.aggregationType = aggregationType;
		this.roundingMode = roundingMode;
		this.processingType = processingType;
		this.processDqSystem = processDqSystem;
		this.exchangeDqSystem = exchangeDqSystem;
	}

}
