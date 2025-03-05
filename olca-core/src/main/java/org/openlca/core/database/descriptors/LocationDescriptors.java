package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.LocationDescriptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LocationDescriptors
		implements DescriptorReader<LocationDescriptor> {

	private final IDatabase db;

	private LocationDescriptors(IDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public static LocationDescriptors of(IDatabase db) {
		return new LocationDescriptors(db);
	}

	@Override
	public IDatabase db() {
		return db;
	}

	public String getCode(ResultSet r) {
		try {
			return r.getString(9);
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'code'", e);
		}
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
					d.data_package,
					d.tags,
					d.code from tbl_locations d""";
	}

	@Override
	public LocationDescriptor getDescriptor(ResultSet r) {
		var d = new LocationDescriptor();
		Util.fill(d, this, r);
		d.code = getCode(r);
		return d;
	}
}
