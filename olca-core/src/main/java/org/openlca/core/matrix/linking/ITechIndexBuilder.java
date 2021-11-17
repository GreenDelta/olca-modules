package org.openlca.core.matrix.linking;

import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;

public interface ITechIndexBuilder {

	TechIndex build(TechFlow refProduct);

}
