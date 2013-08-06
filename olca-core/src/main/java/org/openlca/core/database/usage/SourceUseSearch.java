package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Search for entities where a given source is used. */
class SourceUseSearch implements IUseSearch<SourceDescriptor> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	SourceUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<BaseDescriptor> findUses(SourceDescriptor source) {
		if (source == null)
			return Collections.emptyList();
		String jpql = "select p.id, p.name, p.description, p.processType, p.infrastructureProcess, p.location.id, p.category.id "
				+ " from Process p "
				+ " left join doc.sources s"
				+ " where p.documentation.publication.id = :sourceId or s.id = :sourceId";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql, Collections.singletonMap("sourceId", source.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ProcessDescriptor d = new ProcessDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				d.setProcessType((ProcessType) result[3]);
				d.setInfrastructureProcess((Boolean) result[4]);
				d.setLocation((Long) result[5]);
				d.setCategory((Long) result[6]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search for source use in processes", e);
			return Collections.emptyList();
		}
	}

}
