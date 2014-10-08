package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.Query;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;

/**
 * Searches for the use of LCIA methods in other entities. LCIA methods can be
 * used in projects.
 */
public class ImpactMethodUseSearch implements
		IUseSearch<ImpactMethodDescriptor> {

	private IDatabase database;

	public ImpactMethodUseSearch(IDatabase database) {
		this.database = database;
	}

	public List<BaseDescriptor> findUses(ImpactMethod method) {
		return findUses(Descriptors.toDescriptor(method));
	}

	@Override
	public List<BaseDescriptor> findUses(ImpactMethodDescriptor method) {
		if (method == null)
			return Collections.emptyList();
		String jpql = "select p.id from Project p where p.impactMethodId = :methodId";
		List<Long> idList = Query.on(database).getAll(Long.class, jpql,
				Collections.singletonMap("methodId", method.getId()));
		if (idList.isEmpty())
			return Collections.emptyList();
		HashSet<Long> idSet = new HashSet<>(idList);
		ProjectDao dao = new ProjectDao(database);
		List<ProjectDescriptor> descriptors = dao.getDescriptors(idSet);
		return new ArrayList<BaseDescriptor>(descriptors);
	}

}
