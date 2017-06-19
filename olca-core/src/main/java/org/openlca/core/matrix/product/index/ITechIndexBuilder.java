package org.openlca.core.matrix.product.index;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.ProcessType;

public interface ITechIndexBuilder {

	void setPreferredType(ProcessType preferredType);

	TechIndex build(LongPair refProduct);

	TechIndex build(LongPair refProduct, double demand);

}