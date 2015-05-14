package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.Query;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for the use of sources in other entities. Sources can be used in
 * processes.
 */
public class SourceUseSearch implements IUseSearch<SourceDescriptor> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public SourceUseSearch(IDatabase database) {
		this.database = database;
	}

	public List<BaseDescriptor> findUses(Source source) {
		return findUses(Descriptors.toDescriptor(source));
	}

	@Override
	public List<BaseDescriptor> findUses(SourceDescriptor source) {
		if (source == null)
			return Collections.emptyList();
		String jpql = "select p.id from Process p "
				+ " left join p.documentation.sources s"
				+ " where p.documentation.publication.id = :sourceId "
				+ " or s.id = :sourceId";
		try {
			List<Long> results = Query.on(database).getAll(Long.class, jpql,
					Collections.singletonMap("sourceId", source.getId()));
			HashSet<Long> ids = new HashSet<>();
			ids.addAll(results);
			ProcessDao dao = new ProcessDao(database);
			List<ProcessDescriptor> descriptors = dao.getDescriptors(ids);
			List<BaseDescriptor> list = new ArrayList<>(descriptors.size());
			list.addAll(descriptors);
			return list;
		} catch (Exception e) {
			log.error("Failed to search for source use in processes", e);
			return Collections.emptyList();
		}
	}

}
