package org.openlca.core.database;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessDao extends RootEntityDao<Process, ProcessDescriptor> {

	public ProcessDao(IDatabase database) {
		super(Process.class, ProcessDescriptor.class, database);
	}

	@Override
	protected List<ProcessDescriptor> queryDescriptors(
			String condition, List<Object> params) {
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
						d.process_type,
						d.f_location,
						f.flow_type
				    from tbl_processes d
				    left join tbl_exchanges e on e.id = d.f_quantitative_reference
				    left  join tbl_flows f on e.f_flow = f.id
				""";
		if (condition != null) {
			sql += " " + condition;
		}

		var cons = descriptorConstructor();
		var list = new ArrayList<ProcessDescriptor>();
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

			d.processType = NativeSql.enumItemOf(
					ProcessType.class, r.getString(9));

			var locId = r.getLong(10);
			if (!r.wasNull()) {
				d.location = locId;
			}

			d.flowType = NativeSql.enumItemOf(FlowType.class, r.getString(11));

			list.add(d);
			return true;
		});
		return list;
	}
}
