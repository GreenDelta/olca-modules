package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.Derby;
import org.openlca.core.matrix.LinkingConfig;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.LinkingConfig.DefaultProviders;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;

/**
 * An example for creating a product system with auto-completion.
 */
public class ProductSystemBuilderExample {

	public static void main(String[] args) {
		String dbPath = "C:/Users/Besitzer/openLCA-data-1.4/databases/ecoinvent_2_2_unit";
		IDatabase db = new Derby(new File(dbPath));

		// load the reference process of the new product system
		Process p = new ProcessDao(db).getForRefId(
				"81261285-cc4a-3588-8cce-3aabb786d7aa");

		// create and auto-complete the product system
		LinkingConfig config = new LinkingConfig();
		config.providerLinking = DefaultProviders.PREFER;
		config.preferredType = ProcessType.UNIT_PROCESS;
		ProductSystemBuilder builder = new ProductSystemBuilder(
				MatrixCache.createLazy(db), config);
		ProductSystem system = builder.build(p);

		// save the product system
		new ProductSystemDao(db).insert(system);
	}
}
