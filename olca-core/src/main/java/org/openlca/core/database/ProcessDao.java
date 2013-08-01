package org.openlca.core.database;

import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessDao extends
		CategorizedEntityDao<Process, ProcessDescriptor> {


	public ProcessDao(IDatabase database) {
		super(Process.class, ProcessDescriptor.class, database);
	}

	@Override
	protected String getDescriptorQuery() {
		return "select e.id, e.name, e.description, loc.code "
				+ "from Process e left join e.location loc ";
	}

	@Override
	protected ProcessDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		ProcessDescriptor d = new ProcessDescriptor();
		d.setId((Long) queryResult[0]);
		d.setName((String) queryResult[1]);
		d.setDescription((String) queryResult[2]);
		d.setLocationCode((String) queryResult[3]);
		return d;
	}

}
