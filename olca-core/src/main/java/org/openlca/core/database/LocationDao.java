package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class LocationDao
		extends RootEntityDao<Location, LocationDescriptor> {

	public LocationDao(IDatabase db) {
		super(Location.class, LocationDescriptor.class, db);
	}

	@Override
	protected List<LocationDescriptor> queryDescriptors(String condition, List<Object> params) {
		var sql = """
					select
						d.id,
						d.ref_id,
						d.name,
						d.version,
						d.last_change,
						d.f_category,
						d.library,
						d.tags,
						d.code from
				""" + getEntityTable() + " d";
		if (condition != null) {
			sql += " " + condition;
		}

		var cons = descriptorConstructor();
		var list = new ArrayList<LocationDescriptor>();
		NativeSql.on(db).query(sql, params, r -> {
			var d = cons.get();
			d.id = r.getLong(1);
			d.refId = r.getString(2);
			d.name = r.getString(3);
			d.version = r.getLong(4);
			d.lastChange = r.getLong(5);
			var catId = r.getLong(6);
			if (!r.wasNull()) {
				d.category = catId;
			}
			d.library = r.getString(7);
			d.tags = r.getString(8);
			d.code = r.getString(9);
			list.add(d);
			return true;
		});
		return list;
	}

	/**
	 * Get the location codes from the database in a map: location id -> location
	 * code.
	 */
	public Map<Long, String> getCodes() {
		if (db == null)
			return Collections.emptyMap();
		String sql = "select id, code from tbl_locations";
		Map<Long, String> map = new HashMap<>();
		try {
			NativeSql.on(db).query(sql, r -> {
				map.put(r.getLong(1), r.getString(2));
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return map;
	}
}
