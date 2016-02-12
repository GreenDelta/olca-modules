package org.openlca.geo.kml;

import java.util.List;

import org.openlca.core.matrix.ProductIndex;

public interface IKmlLoader {
	
	List<LocationKml> load(ProductIndex index);

}
