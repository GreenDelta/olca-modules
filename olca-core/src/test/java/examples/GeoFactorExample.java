package examples;

import java.io.File;
import java.util.List;

import org.openlca.core.DataDir;
import org.openlca.core.model.Location;
import org.openlca.geo.lcia.GeoFactorCalculator;
import org.openlca.geo.lcia.GeoFactorSetup;

public class GeoFactorExample {

	public static void main(String[] args) {

		try (var db = DataDir.get().openDatabase("lc-impact-tests")) {
			var setupFile = new File("/home/ms/Desktop/lc-impact-pocp-setup_fixed.json");
			var setup = GeoFactorSetup.read(setupFile, db);
			var afg = db.get(Location.class, "f0357a3f-154b-32ff-a2bf-f55055457068");
			var factors = GeoFactorCalculator.of(db, setup, List.of(afg)).calculate();
			for (var factor : factors) {
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
