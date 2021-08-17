package org.openlca.geo.calc;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.slf4j.LoggerFactory;

public final class PrecisionReduction {

	private PrecisionReduction() {
	}

	public static FeatureCollection run(FeatureCollection coll, int decimalPlaces) {
		if (coll == null)
			return new FeatureCollection();

		// set up the reducer
		PrecisionModel pm = new PrecisionModel(
				Math.pow(10, decimalPlaces));
		GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(pm);

		// run the reduction
		List<Feature> features = coll.features.parallelStream()
				.map(f -> {
					if (f.geometry == null)
						return f.copy();
					Feature mapped = new Feature();
					if (f.properties != null) {
						mapped.properties = new HashMap<>(f.properties);
					}
					if (f.geometry == null)
						return mapped;
					Geometry g = JTS.fromGeoJSON(f.geometry);
					if (g == null)
						return mapped;
					try {
						Geometry reduced = reducer.reduce(g);
						mapped.geometry = JTS.toGeoJSON(reduced);
					} catch (Exception e) {
						LoggerFactory.getLogger(PrecisionReduction.class).error(
								"precision reduction failed for " + g, e);
						mapped.geometry = f.geometry.copy();
					}
					return mapped;
				})
				.collect(Collectors.toList());

		// build the result
		FeatureCollection r = new FeatureCollection();
		r.features.addAll(features);
		return r;
	}
}
