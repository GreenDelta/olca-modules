package org.openlca.core.matrix.product.index;

import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;

public interface ITechIndexBuilder {

	TechIndex build(ProcessProduct refProduct);

	TechIndex build(ProcessProduct refProduct, double demand);

}