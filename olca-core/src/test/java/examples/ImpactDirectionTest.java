package examples;

import org.openlca.core.database.Derby;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Direction;
import org.openlca.util.Categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ImpactDirectionTest {

	public static void main(String[] args) {
		try (var db = Derby.fromDataDir("refdat")) {

			var flowDirs = flowDirectionsOf(db);
			var impactDirs = new HashMap<Long, ImpactDirection>();
			var errors = new ArrayList<Error>();
			var sql = "select f_impact_category, f_flow, value" +
				" from tbl_impact_factors";

			NativeSql.on(db).query(sql, r -> {

				var factor = r.getDouble(3);
				if (factor == 0)
					return true;

				var impact = r.getLong(1);
				var flow = r.getLong(2);
				var flowDir = flowDirs.get(flow);

				var impactDir = impactDirs.get(impact);
				if (impactDir == null) {
					impactDirs.put(impact, ImpactDirection.of(factor, flowDir));
				} else if (!(impactDir.matches(factor, flowDir))) {
					errors.add(new Error(impact, flow));
				}
				return true;
			});

			if (errors.isEmpty()) {
				System.out.println("no errors!");
				return;
			}

			var flows = new FlowDao(db).descriptorMap();
			var impacts = new ImpactCategoryDao(db).descriptorMap();
			System.out.printf("found %d errors:%n", errors.size());
			for (var err : errors) {
				var flow = flows.get(err.flow);
				var impact = impacts.get(err.impact);
				System.out.printf(
					"  flow %s (%s) in impact category %s (%s)%n",
					flow.refId, flow.name, impact.refId, impact.name);
			}
		}


	}

	private static Map<Long, Direction> flowDirectionsOf(IDatabase db) {
		var categories = Categories.pathsOf(db);
		var sql = "select id, f_category from tbl_flows";
		var flowDirs = new HashMap<Long, Direction>();
		NativeSql.on(db).query(sql, r -> {
			var path = categories.pathOf(r.getLong(2));
			var opt = directionOf(path);
			if (opt.isPresent()) {
				var flowId = r.getLong(1);
				flowDirs.put(flowId, opt.get());
			}
			return true;
		});
		return flowDirs;
	}

	private static Optional<Direction> directionOf(String path) {
		if (path == null)
			return Optional.empty();
		var parts = path.trim().toLowerCase().split("/");
		var first = parts[0];
		if (first.equals("elementary flows")) {
			if (parts.length == 1)
				return Optional.empty();
			first = parts[1];
		}
		return switch (first) {
			case "emission to air",
				"emission to soil",
				"emission to water",
				"immaterial emission",
				"waste" -> Optional.of(Direction.OUTPUT);
			case "resource" -> Optional.of(Direction.INPUT);
			default -> {
				System.out.println("could not determine direction of " + first);
				yield Optional.empty();
			}
		};
	}

	record ImpactDirection(Direction direction) {

		static ImpactDirection of(double factor, Direction d) {
			return factor < 0
				? new ImpactDirection(otherOf(d))
				: new ImpactDirection(d);
		}

		private static Direction otherOf(Direction d) {
			return d == Direction.INPUT
				? Direction.OUTPUT
				: Direction.INPUT;
		}

		boolean matches(double factor, Direction d) {
			if (factor == 0)
				return true;
			return factor < 0
				? otherOf(d) == direction
				: d == direction;
		}

	}

	record Error(long impact, long flow) {
	}

}
