package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.ParameterDescriptor;

import java.sql.ResultSet;

public class ParameterDescriptors
		implements DescriptorReader<ParameterDescriptor> {

	private final IDatabase db;

	private ParameterDescriptors(IDatabase db) {
		this.db = db;
	}

	public static ParameterDescriptors of(IDatabase db) {
		return new ParameterDescriptors(db);
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
				 	d.tags from tbl_parameters
				 	where d.scope = '"""
				+ ParameterScope.GLOBAL.name() + "'";
	}

	@Override
	public ParameterDescriptor getDescriptor(ResultSet r) {
		var d = new ParameterDescriptor();
		Util.fill(d, this, r);
		return d;
	}
}
