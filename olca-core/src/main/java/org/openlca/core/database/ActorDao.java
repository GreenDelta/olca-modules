package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class ActorDao extends BaseDao<Actor> implements IRootEntityDao<Actor> {

	public ActorDao(EntityManagerFactory emf) {
		super(Actor.class, emf);
	}

	@Override
	public List<ActorDescriptor> getDescriptors(Category category) {
		String jpql = "select a.id, a.name, a.description from Actor a "
				+ "where a.category = :category";
		log.trace("get actor descriptors for {}", category);
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("category", category));
			return toDescriptors(results);
		} catch (Exception e) {
			log.error("Failed to get actor descriptors for category "
					+ category, e);
			return Collections.emptyList();
		}
	}

	private List<ActorDescriptor> toDescriptors(List<Object[]> results) {
		List<ActorDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ActorDescriptor descriptor = new ActorDescriptor();
			descriptor.setId((String) result[0]);
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
