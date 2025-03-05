package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessDescriptors
		implements DescriptorReader<ProcessDescriptor> {

	private final IDatabase db;

	private ProcessDescriptors(IDatabase db) {
		this.db = db;
	}

	public static ProcessDescriptors of(IDatabase db) {
		return new ProcessDescriptors(db);
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
						d.data_package,
						d.tags,
						d.process_type,
						d.f_location,
						f.flow_type
				    from tbl_processes d
				    left join tbl_exchanges e on e.id = d.f_quantitative_reference
				    left  join tbl_flows f on e.f_flow = f.id
				""";
	}

	public ProcessType getProcessType(ResultSet r) {
		try {
			var s = r.getString(9);
			return s != null
					? Enum.valueOf(ProcessType.class, s)
					: null;
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'process-type'", e);
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

	public FlowType getFlowType(ResultSet r) {
		try {
			var s = r.getString(11);
			return s != null
					? Enum.valueOf(FlowType.class, s)
					: null;
		} catch (SQLException e) {
			throw Util.ex("failed to read field 'flow-type'", e);
		}
	}

	@Override
	public ProcessDescriptor getDescriptor(ResultSet r) {
		var d = new ProcessDescriptor();
		Util.fill(d, this, r);
		d.processType = getProcessType(r);
		d.flowType = getFlowType(r);
		d.location = getLocation(r);
		return d;
	}
}
