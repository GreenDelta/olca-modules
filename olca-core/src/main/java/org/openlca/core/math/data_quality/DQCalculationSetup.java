package org.openlca.core.math.data_quality;

import java.math.RoundingMode;

import org.openlca.core.model.DQSystem;

public class DQCalculationSetup {

	public long productSystemId;
	public AggregationType aggregationType;
	public RoundingMode roundingMode;
	public ProcessingType processingType;
	public DQSystem processDqSystem;
	public DQSystem exchangeDqSystem;

}
