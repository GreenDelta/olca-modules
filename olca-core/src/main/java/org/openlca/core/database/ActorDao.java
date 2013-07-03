package org.openlca.core.database;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Actor;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class ActorDao extends CategorizedEnitityDao<Actor> {

	public ActorDao(EntityManagerFactory emf) {
		super(Actor.class, emf);
	}

	public List<BaseDescriptor> whereUsed(Actor actor) {
		return new ActorUseSearch(getEntityFactory()).findUses(actor);
	}
}
