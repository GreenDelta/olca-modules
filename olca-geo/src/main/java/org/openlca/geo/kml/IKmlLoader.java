package org.openlca.geo.kml;

import java.util.List;

import org.openlca.core.matrix.TechIndex;

public interface IKmlLoader {

	List<LocationKml> load(TechIndex index);

}
