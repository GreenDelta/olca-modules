package examples;

import org.openlca.core.DataDir;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.geo.calc.JTS;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.lcia.GeoFactorCalculator;
import org.openlca.geo.lcia.GeoFactorSetup;

import java.io.File;
import java.util.List;

public class GeoFactorExample {

	public static void main(String[] args) {

		try (var db = DataDir.get().openDatabase("lc-impact-tests")) {
			var setupFile = new File("/home/ms/Desktop/lc-impact-pocp-setup.json");

			var setup = GeoFactorSetup.read(setupFile, db);

			var fixedFeatures = new FeatureCollection();
			setup.features.features.forEach(f -> {
				var jts = JTS.fromGeoJSON(f.geometry).buffer(0);
				var feature = new Feature();
				feature.geometry = JTS.toGeoJSON(jts);
				feature.properties = f.properties;
				fixedFeatures.features.add(feature);
			});

			var fixedSetup = new GeoFactorSetup(fixedFeatures);
			fixedSetup.bindings.addAll(setup.bindings);
			fixedSetup.properties.addAll(setup.properties);

			var impact = db.get(ImpactCategory.class, "39ec61cf-a337-4535-b36b-98b5dee816f5");
			var afg = db.get(Location.class, "f0357a3f-154b-32ff-a2bf-f55055457068");
			var calc = new GeoFactorCalculator(db, fixedSetup, impact, List.of(afg));
			calc.run();

			for (var factor : impact.impactFactors) {
				var loc = factor.location != null
						? factor.location.code
						: "GLO";
				System.out.printf("%s - %s : %.4e%n",
						factor.flow.name,
						loc,
						factor.value
				);
			}
		}
	}
}
