package org.openlca.core.database;

import org.openlca.core.model.ProcessGroupSet;

public class ProcessGroupSetDao extends BaseDao<ProcessGroupSet> {

	public ProcessGroupSetDao(IDatabase database) {
		super(ProcessGroupSet.class, database);
	}

}
