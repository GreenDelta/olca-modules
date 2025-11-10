package org.openlca.core.database.descriptors;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Direction;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactDescriptors
		implements DescriptorReader<ImpactDescriptor> {

	private final IDatabase db;

	private ImpactDescriptors(IDatabase db) {
		this.db = db;
	}

	public static ImpactDescriptors of(IDatabase db) {
		return new ImpactDescriptors(db);
	}

	@Override
	public IDatabase db() {
		return db;
	}

	@Override
	public String query() {
		return """
				select
					d.id,
					d.ref_id,
					d.name,
					d.version,
					d.last_change,
					d.f_category,
					d.library,
					d.tags,
					d.reference_unit,
					d.direction from tbl_impact_categories d""";
	}

	public String getReferenceUnit(ResultSet r) {
		try {
			return r.getString(9);
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'reference-unit'", e);
		}
	}

	public Direction getDirection(ResultSet r) {
		try {
			var s = r.getString(10);
			return s != null
					? Direction.from(s)
					: null;
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'direction'", e);
		}
	}

	@Override
	public ImpactDescriptor getDescriptor(ResultSet r) {
		var d = new ImpactDescriptor();
		Util.fill(d, this, r);
		d.referenceUnit = getReferenceUnit(r);
		d.direction = getDirection(r);
		return d;
	}
}
