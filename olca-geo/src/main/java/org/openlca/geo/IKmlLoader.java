package org.openlca.geo;

import java.util.Map;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;

public interface IKmlLoader {
	
	Map<LongPair, KmlFeature> load(ProductIndex productIndex);

}
