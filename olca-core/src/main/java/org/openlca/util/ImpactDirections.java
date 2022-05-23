package org.openlca.util;

import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Direction;
import org.slf4j.LoggerFactory;

public final class ImpactDirections {

	private ImpactDirections() {
	}

	/**
	 * Tries to infer and update the impact directions of the LCIA categories in
	 * the given database. This is an experimental utility function to upgrade
	 * databases and probably can be deleted later.
	 */
	public static void inferAndUpdate(IDatabase db) {
		var log = LoggerFactory.getLogger(ImpactDirections.class);

		// determine the directions from flow categories
		log.info("determine impact directions");
		var paths = Categories.pathsOf(db);
		var directions = new TLongLongHashMap();
		var query = """
			select fac.f_impact_category, flow.f_category
			from tbl_impact_factors fac
			inner join tbl_flows flow
			on fac.f_flow = flow.id
			""";
		NativeSql.on(db).query(query, r -> {
			var path = paths.pathOf(r.getLong(2));
			if (path == null)
				return true;
			long dir = path.toLowerCase().contains("resource")
				? -1
				: 1;
			var impact = r.getLong(1);
			directions.adjustOrPutValue(impact, dir, dir);
			return true;
		});

		// update the impact table
		var update = """
			select id, name, direction from tbl_impact_categories
			""";
		NativeSql.on(db).updateRows(update, r -> {
			var current = r.getString(3);
			if (Strings.notEmpty(current))
				return true;
			var id = r.getLong(1);
			var name = r.getString(2);
			long dir = directions.get(id);
			if (dir == 0) {
				log.warn("could not determine impact" +
					" direction of {} [id={}]", name, id);
				return true;
			}
			var direction = dir > 0
				? Direction.OUTPUT
				: Direction.INPUT;
			log.info("set impact direction of " +
				"{} to: {}", name, direction);
			r.updateString(3, direction.name());
			r.updateRow();
			return true;
		});
	}
}
