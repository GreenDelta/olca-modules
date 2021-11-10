package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.Derby;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.SimpleResult;

/*
 * This example shows how you can create and calculate a
 * product system without storing it in the database.
 */
public class ProductSystemInMemoryCalculationExample {
	public static void main(String[] args) throws Exception {

		// load the database and matrix cache
		String workspace = "C:/Users/Besitzer/openLCA-data-1.4";
		String dbPath = workspace + "/databases/ecoinvent_2_2_unit";
		IDatabase db = new Derby(new File(dbPath));
		MatrixCache mcache = MatrixCache.createLazy(db);

		// load the reference process and create
		// the product system with auto-completion
		// the system is not saved in the database
		Process p = new ProcessDao(db).getForRefId(
				"7ff672e3-a296-30e8-b1bb-a3173711a28b");
		var config = new LinkingConfig()
			.providerLinking(ProviderLinking.PREFER_DEFAULTS)
			.preferredType(ProcessType.UNIT_PROCESS);
		var builder = new ProductSystemBuilder(mcache, config);
		var system = builder.build(p);
		var method = db.get(ImpactMethod.class,
			"207ffac9-aaa8-401d-ac90-874defd3751a");

		// create the calculation setup
		var setup = CalculationSetup.simple(system)
			.withImpactMethod(method);

		// load the native library and calculate the result
		// TODO: load Julia libraries first here
		SystemCalculator calc = new SystemCalculator(db);
		SimpleResult r = calc.calculateSimple(setup);

		// print the LCIA results
		for (ImpactDescriptor impact : r.getImpacts()) {
			System.out.println(impact.name + "\t"
					+ r.getTotalImpactResult(impact) + "\t"
					+ impact.referenceUnit);
		}

		// finally, close the database
		db.close();
	}
}
