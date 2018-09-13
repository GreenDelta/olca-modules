package org.openlca.core.matrix.product.index;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;

public interface ITechIndexBuilder {

	TechIndex build(LongPair refProduct);

	TechIndex build(LongPair refProduct, double demand);

}