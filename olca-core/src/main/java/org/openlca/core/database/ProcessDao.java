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

import com.google.common.base.Optional;

public class ProcessDao extends BaseDao<Process> implements
		IRootEntityDao<Process> {

	private Logger log = LoggerFactory.getLogger(getClass());

	public ProcessDao(EntityManagerFactory emf) {
		super(Process.class, emf);
	}

	@Override
	public List<ProcessDescriptor> getDescriptors(Optional<Category> category) {
		String jpql = "select p.id, p.name, p.description, loc.code "
				+ "from Process p left join p.location loc ";
		Map<String, Category> params = null;
		if (category.isPresent()) {
			jpql += "where p.category = :category";
			params = Collections.singletonMap("category", category.get());
		} else {
			jpql += "where p.category is null";
			params = Collections.emptyMap();
		}
		return runDescriptorQuery(jpql, params);
	}

	private List<ProcessDescriptor> runDescriptorQuery(String jpql,
			Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql, params);
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
		} catch (Exception e) {
			log.error("failed to get process descriptors " + params, e);
			return Collections.emptyList();
		}
	}

	public List<BaseDescriptor> whereUsed(Process process) {
		if (process == null || process.getRefId() == null)
			return Collections.emptyList();
		String jpql = "select s.id, s.name, s.description from ProductSystem s "
				+ "join s.processes p where p.id = :processId";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("processId", process.getRefId()));
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
