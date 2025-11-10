package org.openlca.core.database.descriptors;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowDescriptors
		implements DescriptorReader<FlowDescriptor> {

	private final IDatabase db;

	private FlowDescriptors(IDatabase db) {
		this.db = db;
	}

	public static FlowDescriptors of(IDatabase db) {
		return new FlowDescriptors(db);
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
					d.flow_type,
					d.f_location,
					d.f_reference_flow_property	from tbl_flows d""";
	}

	public FlowType getFlowType(ResultSet r) {
		try {
			var s = r.getString(9);
			return s != null
					? Enum.valueOf(FlowType.class, s)
					: null;
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'flow-type'", e);
		}
	}

	public Long getLocation(ResultSet r) {
		try {
			var loc = r.getLong(10);
			return r.wasNull() ? null : loc;
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'location'", e);
		}
	}

	public long getRefFlowProperty(ResultSet r) {
		try {
			var prop = r.getLong(11);
			return r.wasNull() ? 0L : prop;
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'ref.-flow-property", e);
		}
	}

	@Override
	public FlowDescriptor getDescriptor(ResultSet r) {
		var d = new FlowDescriptor();
		Util.fill(d, this, r);
		d.flowType = getFlowType(r);
		d.location = getLocation(r);
		d.refFlowPropertyId = getRefFlowProperty(r);
		return d;
	}
}
