package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.enums.DistributionParameterType;
import org.openlca.simapro.csv.model.enums.DistributionType;

public interface IDistribution {

	double getDistributionParameter(DistributionParameterType type);

	DistributionType getType();

}
