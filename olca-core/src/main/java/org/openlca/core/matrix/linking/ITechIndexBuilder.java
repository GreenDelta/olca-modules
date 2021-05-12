package org.openlca.core.matrix.linking;

import org.openlca.core.matrix.index.ProcessProduct;
import org.openlca.core.matrix.index.TechIndex;

public interface ITechIndexBuilder {

	TechIndex build(ProcessProduct refProduct);

	TechIndex build(ProcessProduct refProduct, double demand);

}
