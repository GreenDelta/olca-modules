package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class ActorDao extends BaseDao<Actor> {

	public ActorDao(EntityManagerFactory emf) {
		super(Actor.class, emf);
	}

	public List<ActorDescriptor> getDescriptors(Category category)
			throws Exception {
		String categoryId = category == null ? null : category.getId();
		String jpql = "select a.id, a.name, a.description from Actor a where a.categoryId = :categoryId";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql,
				Collections.singletonMap("categoryId", categoryId));
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
