package examples;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.geo.calc.FeatureValidation;
import org.openlca.geo.lcia.GeoFactorSetup;

public class FeatureValidationExample {

	public static void main(String[] args) {
		try (var db = DataDir.get().openDatabase("lc-impact-tests")) {
			var setupFile = new File("/home/ms/Desktop/lc-impact-pocp-setup.json");
			var setup = GeoFactorSetup.read(setupFile, db);
			var validation = FeatureValidation.of(setup.features);
			validation.run();
			System.out.println(validation.stats());
		}
	}
}
