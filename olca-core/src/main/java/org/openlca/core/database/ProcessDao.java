package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDao extends BaseDao<Process> {

	private Logger log = LoggerFactory.getLogger(getClass());

	public ProcessDao(EntityManagerFactory emf) {
		super(Process.class, emf);
	}

	public List<ProcessDescriptor> getDescriptors(Category category)
			throws Exception {
		String categoryId = category == null ? null : category.getId();
		String jpql = "select p.id, p.name, p.description, loc.code "
				+ "from Process p left join p.location loc "
				+ "where p.categoryId = :categoryId";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql,
				Collections.singletonMap("categoryId", categoryId));
		List<ProcessDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ProcessDescriptor d = new ProcessDescriptor();
			d.setId((String) result[0]);
			d.setName((String) result[1]);
			d.setDescription((String) result[2]);
			d.setLocationCode((String) result[3]);
			descriptors.add(d);
		}
		return descriptors;
	}

	public List<BaseDescriptor> whereUsed(Process process) {
		if (process == null || process.getId() == null)
			return Collections.emptyList();
		String jpql = "select s.id, s.name, s.description from ProductSystem s "
				+ "join s.processes p where p.id = :processId";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("processId", process.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ProductSystemDescriptor d = new ProductSystemDescriptor();
				d.setId((String) result[0]);
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

	public ProcessDescriptor getDescriptor(String processId) throws Exception {
		String jpql = "select p.name, p.description, loc.code "
				+ "from Process p left join p.location loc where p.id = "
				+ ":processId";
		Map<String, String> params = Collections.singletonMap("processId",
				processId);
		Object[] result = query().getFirst(Object[].class, jpql, params);
		if (result == null)
			return null;
		ProcessDescriptor d = new ProcessDescriptor();
		d.setId(processId);
		d.setName((String) result[0]);
		d.setDescription((String) result[1]);
		d.setLocationCode((String) result[2]);
		return d;
	}

}
