package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDao extends CategorizedEnitityDao<Process> {

	private Logger log = LoggerFactory.getLogger(getClass());

	public ProcessDao(IDatabase database) {
		super(Process.class, database);
	}

	@Override
	protected String getDescriptorQuery() {
		return "select e.id, e.name, e.description, loc.code "
				+ "from Process e left join e.location loc ";
	}

	@Override
	protected BaseDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		ProcessDescriptor d = new ProcessDescriptor();
		d.setId((Long) queryResult[0]);
		d.setName((String) queryResult[1]);
		d.setDescription((String) queryResult[2]);
		d.setLocationCode((String) queryResult[3]);
		return d;
	}

	public List<BaseDescriptor> whereUsed(Process process) {
		if (process == null || process.getRefId() == null)
			return Collections.emptyList();
		String jpql = "select s.id, s.name, s.description from ProductSystem s "
				+ "join s.processes p where p.id = :processId";
		try {
			List<Object[]> results = Query.on(getDatabase()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("processId", process.getRefId()));
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
