package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.Derby;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;

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
		var config = new LinkingConfig()
			.providerLinking(ProviderLinking.PREFER_DEFAULTS)
			.preferredType(ProcessType.UNIT_PROCESS);
		var system = new ProductSystemBuilder(MatrixCache.createLazy(db), config)
			.build(p);

		// save the product system
		new ProductSystemDao(db).insert(system);
	}
}
