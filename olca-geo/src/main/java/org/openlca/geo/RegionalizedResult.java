package org.openlca.geo;

import java.util.List;

import org.openlca.core.results.FullResult;
import org.openlca.geo.kml.LocationKml;
import org.openlca.geo.parameter.ParameterSet;

public class RegionalizedResult {

	public final FullResult result;
	public final List<LocationKml> kmlData;
	public final ParameterSet parameterSet;

	RegionalizedResult(FullResult result, List<LocationKml> kmlData,
			ParameterSet parameterSet) {
		this.result = result;
		this.kmlData = kmlData;
		this.parameterSet = parameterSet;
	}

}
