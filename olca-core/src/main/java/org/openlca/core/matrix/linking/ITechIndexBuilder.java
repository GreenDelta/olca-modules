package org.openlca.core.matrix.linking;

import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechFlowIndex;

public interface ITechIndexBuilder {

	TechFlowIndex build(TechFlow refProduct);

	TechFlowIndex build(TechFlow refProduct, double demand);

}
