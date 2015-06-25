package org.openlca.core.matrix.product.index;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.model.ProcessType;

public interface IProductIndexBuilder {

	void setPreferredType(ProcessType preferredType);

	ProductIndex build(LongPair refProduct);

	ProductIndex build(LongPair refProduct, double demand);

}