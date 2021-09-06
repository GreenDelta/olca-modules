package examples;

import java.io.File;

import org.openlca.core.database.Derby;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.io.NpyMatrix;

public class ImpactBuilderExample {

	public static void main(String[] args) throws Exception {
		try (var db = Derby.fromDataDir("ei22")) {
			var impactIndex = ImpactIndex.of(db);
			var enviIndex = EnviIndex.create(db, impactIndex);
			var data = ImpactBuilder.of(db, enviIndex)
				.withImpacts(impactIndex)
				.build();
			NpyMatrix.write(
				new File("./target"), "impact_factors", data.impactMatrix);
		}
	}
}
