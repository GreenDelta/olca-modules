package examples;

import java.io.File;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.model.FlowType;
import org.openlca.expressions.FormulaInterpreter;

public class ImpactBuilderExample {

	public static void main(String[] args) throws Exception {
		String dbPath = "C:/Users/ms/openLCA-data-1.4/databases" +
				"/openlca_lcia_v2_0_5_under_dev_201911220";
		IDatabase db = new DerbyDatabase(new File(dbPath));

		// build the LCIA category and flow indices
		var impactIndex = new ImpactIndex();
		new ImpactCategoryDao(db).getDescriptors().forEach(
				impactIndex::put);

		// !Note that this is just an example. It sets all flows to output
		// flows. Normally, the flow direction would be determined from
		// the inventory
		FlowIndex flowIndex = FlowIndex.create();
		new FlowDao(db).getDescriptors().forEach(d -> {
			if (d.flowType == FlowType.ELEMENTARY_FLOW) {
				flowIndex.putOutput(d);
			}
		});

		ImpactBuilder.ImpactData data = new ImpactBuilder(db).build(
				flowIndex, impactIndex, new FormulaInterpreter());
		Npy.save(new File("impact_factors.npy"), data.impactMatrix);
		db.close();
	}

}
