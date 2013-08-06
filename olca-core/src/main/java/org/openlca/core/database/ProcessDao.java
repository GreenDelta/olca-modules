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
		return new String[] { "id", "ref_id", "name", "description",
				"process_type", "infrastructure_process", "f_category",
				"f_location" };
	}

	@Override
	protected ProcessDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		ProcessDescriptor d = super.createDescriptor(queryResult);
		d.setProcessType(ProcessType.valueOf((String) queryResult[4]));
		d.setInfrastructureProcess((Integer) queryResult[5] == 1);
		d.setCategory((Long) queryResult[6]);
		d.setLocation((Long) queryResult[7]);
		return d;
	}
}
