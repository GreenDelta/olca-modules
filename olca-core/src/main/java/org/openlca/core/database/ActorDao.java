package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.Actor;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class ActorDao extends CategorizedEnitityDao<Actor, ActorDescriptor> {

	public ActorDao(IDatabase database) {
		super(Actor.class, ActorDescriptor.class, database);
	}

	public List<BaseDescriptor> whereUsed(Actor actor) {
		return new ActorUseSearch(getDatabase()).findUses(actor);
	}
}
