package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.Query;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;

class ImpactMethodUseSearch implements IUseSearch<ImpactMethodDescriptor> {

	private IDatabase database;

	public ImpactMethodUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<BaseDescriptor> findUses(ImpactMethodDescriptor impactMethod) {
		if (impactMethod == null)
			return Collections.emptyList();
		String jpql = "select p.id from Project p where p.impactMethodId = :methodId";
		List<Long> idList = Query.on(database).getAll(Long.class, jpql,
				Collections.singletonMap("methodId", impactMethod.getId()));
		if (idList.isEmpty())
			return Collections.emptyList();
		HashSet<Long> idSet = new HashSet<>();
		idSet.addAll(idList);
		ProjectDao dao = new ProjectDao(database);
		List<ProjectDescriptor> descriptors = dao.getDescriptors(idSet);
		List<BaseDescriptor> list = new ArrayList<>(descriptors.size() + 1);
		list.addAll(descriptors);
		return list;
	}

}
