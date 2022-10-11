package examples;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.results.EachOneResult;

public class EachOneResultExample {

	public static void main(String[] args) {
		try (var db = DataDir.get().openDatabase("ei22")) {
			if (db.getVersion() < IDatabase.CURRENT_VERSION) {
				Upgrades.on(db);
			}
			int i = 0;
			for (var pair : EachOneResult.of(db).get()) {
				var product = pair.first;
				var result = pair.second;
				System.out.printf("Calculated result for %s%n%n", product.provider().refId);
				result.impactIndex().each((_i, impact) -> {
					var r = result.totalImpactOf(impact);
					System.out.printf("%s;%.5f;%s%n",
						impact.name, r, impact.referenceUnit);
				});

				i++;
				if (i > 3)
					break;
			}

		}
	}
}
