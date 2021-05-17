package examples;

import java.io.File;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.Derby;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.model.FlowType;

public class ImpactBuilderExample {

	public static void main(String[] args) throws Exception {
		var db = Derby.fromDataDir(
			"openlca_lcia_v2_0_5_under_dev_201911220");

		// build the LCIA category and flow indices
		var impactIndex = ImpactIndex.of(db);

		// !Note that this is just an example. It sets all flows to output
		// flows. Normally, the flow direction would be determined from
		// the inventory
		var flowIndex = EnviIndex.create();
		new FlowDao(db).getDescriptors().forEach(d -> {
			if (d.flowType == FlowType.ELEMENTARY_FLOW) {
				flowIndex.add(EnviFlow.outputOf(d));
			}
		});

		var data = ImpactBuilder.of(db, flowIndex)
				.withImpacts(impactIndex)
				.build();
		Npy.save(new File("impact_factors.npy"), data.impactMatrix);
		db.close();
	}
}
