package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Searches for models where a given actor is used. */
class ActorUseSearch implements IUseSearch<ActorDescriptor> {

	private IDatabase database;
	private Logger log = LoggerFactory.getLogger(getClass());

	ActorUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<BaseDescriptor> findUses(ActorDescriptor actor) {
		List<BaseDescriptor> processDescriptors = findInProcesses(actor);
		List<BaseDescriptor> projectDescriptors = findInProjects(actor);
		List<BaseDescriptor> results = new ArrayList<>(
				processDescriptors.size() + projectDescriptors.size() + 2);
		results.addAll(processDescriptors);
		results.addAll(projectDescriptors);
		return results;
	}

	private List<BaseDescriptor> findInProjects(ActorDescriptor actor) {
		try {
			String jpql = "select p.id, p.name, p.description from Project p "
					+ "where p.author.id = :actorId";
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql, Collections.singletonMap("actorId", actor.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ProjectDescriptor d = new ProjectDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search for actor uses in projects", e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> findInProcesses(ActorDescriptor actor) {
		try {
			String jpql = "select p.id, p.name, p.description, p.processType, p.infrastructureProcess, p.location.id, p.category.id, p.quantitativeReference.id "
					+ " from Process p "
					+ " where p.documentation.reviewer.id = :actorId "
					+ " or p.documentation.dataSetOwner.id = :actorId "
					+ " or p.documentation.dataGenerator.id = :actorId "
					+ " or p.documentation.dataDocumentor.id = :actorId";
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql, Collections.singletonMap("actorId", actor.getId()));
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
				d.setQuantitativeReference((Long) result[7]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search for actor uses in processes", e);
			return Collections.emptyList();
		}
	}
}
