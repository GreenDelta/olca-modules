package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.LinkingConfig;
import org.openlca.core.matrix.LinkingConfig.DefaultProviders;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.DenseSolver;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.eigen.NativeLibrary;

/*
 * This example shows how you can create and calculate a
 * product system without storing it in the database.
 */
public class ProductSystemInMemoryCalculationExample {
	public static void main(String[] args) throws Exception {

		// load the database and matrix cache
		String workspace = "C:/Users/Besitzer/openLCA-data-1.4";
		String dbPath = workspace + "/databases/ecoinvent_2_2_unit";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		MatrixCache mcache = MatrixCache.createLazy(db);

		// load the reference process and create
		// the product system with auto-completion
		// the system is not saved in the database
		Process p = new ProcessDao(db).getForRefId(
				"7ff672e3-a296-30e8-b1bb-a3173711a28b");
		LinkingConfig config = new LinkingConfig();
		config.providerLinking = DefaultProviders.PREFER;
		config.preferredType = ProcessType.UNIT_PROCESS;
		ProductSystemBuilder builder = new ProductSystemBuilder(
				mcache, config);
		ProductSystem system = builder.build(p);

		// create the calculation setup
		ImpactMethodDescriptor method = new ImpactMethodDao(db)
				.getDescriptorForRefId("207ffac9-aaa8-401d-ac90-874defd3751a");
		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, system);
		setup.impactMethod = method;

		// load the native library and calculate the result
		NativeLibrary.loadFromDir(new File(workspace));
		SystemCalculator calc = new SystemCalculator(
				mcache, new DenseSolver());
		SimpleResult r = calc.calculateSimple(setup);

		// print the LCIA results
		for (ImpactCategoryDescriptor impact : r.getImpacts()) {
			System.out.println(impact.name + "\t"
					+ r.getTotalImpactResult(impact) + "\t"
					+ impact.referenceUnit);
		}

		// finally, close the database
		db.close();
	}
}
