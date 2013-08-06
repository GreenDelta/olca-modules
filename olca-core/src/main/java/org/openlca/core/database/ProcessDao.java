package org.openlca.core.database;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessDao extends
		CategorizedEntityDao<Process, ProcessDescriptor> {

	public ProcessDao(IDatabase database) {
		super(Process.class, ProcessDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "name", "description", "process_type",
				"infrastructure_process", "f_category", "f_location" };
	}

	@Override
	protected ProcessDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		ProcessDescriptor d = new ProcessDescriptor();
		d.setId((Long) queryResult[0]);
		d.setName((String) queryResult[1]);
		d.setDescription((String) queryResult[2]);
		d.setProcessType((ProcessType) queryResult[3]);
		d.setInfrastructureProcess((Boolean) queryResult[4]);
		d.setCategory((Long) queryResult[5]);
		d.setLocation((Long) queryResult[6]);
		return d;
	}

}
