package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for the use of processes in other entities. Processes can be used in
 * product systems.
 */
public class ProcessUseSearch implements IUseSearch<ProcessDescriptor> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public ProcessUseSearch(IDatabase database) {
		this.database = database;
	}

	public List<BaseDescriptor> findUses(Process process) {
		return findUses(Descriptors.toDescriptor(process));
	}

	@Override
	public List<BaseDescriptor> findUses(ProcessDescriptor process) {
		if (process == null)
			return Collections.emptyList();
		String jpql = "select s.id, s.name, s.description from ProductSystem s "
				+ "where :processId MEMBER OF s.processes";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql,
					Collections.singletonMap("processId", process.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ProductSystemDescriptor d = new ProductSystemDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Faile to search for processes in product systems", e);
			return Collections.emptyList();
		}
	}

}
