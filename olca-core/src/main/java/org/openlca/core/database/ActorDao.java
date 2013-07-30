package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.Actor;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class ActorDao extends CategorizedEnitityDao<Actor> {

	public ActorDao(IDatabase database) {
		super(Actor.class, database);
	}

	public List<BaseDescriptor> whereUsed(Actor actor) {
		return new ActorUseSearch(getDatabase()).findUses(actor);
	}
}
