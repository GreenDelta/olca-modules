package org.openlca.core.matrix.product.index;

import org.openlca.core.matrix.Provider;
import org.openlca.core.matrix.TechIndex;

public interface ITechIndexBuilder {

	TechIndex build(Provider refProduct);

	TechIndex build(Provider refProduct, double demand);

}