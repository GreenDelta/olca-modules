package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.common.base.Optional;

public class ActorDao extends RootEntityDao<Actor> {

	public ActorDao(EntityManagerFactory emf) {
		super(Actor.class, emf);
	}

	@Override
	public List<ActorDescriptor> getDescriptors(Optional<Category> category) {
		String jpql = "select a.id, a.name, a.description from Actor a ";
		Map<String, Category> params = null;
		if (category.isPresent()) {
			jpql += "where a.category = :category";
			params = Collections.singletonMap("category", category.get());
		} else {
			jpql += "where a.category is null";
			params = Collections.emptyMap();
		}
		return runDescriptorQuery(jpql, params);
	}

	private List<ActorDescriptor> runDescriptorQuery(String jpql,
			Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql, params);
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("Failed to get actor descriptors " + params, e);
			return Collections.emptyList();
		}
	}

	private List<ActorDescriptor> createDescriptors(List<Object[]> results) {
		List<ActorDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ActorDescriptor descriptor = new ActorDescriptor();
			descriptor.setId((Long) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

	public List<BaseDescriptor> whereUsed(Actor actor) {
		return new ActorUseSearch(getEntityFactory()).findUses(actor);
	}
}
