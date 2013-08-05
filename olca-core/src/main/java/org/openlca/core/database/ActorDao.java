package org.openlca.core.database;

import org.openlca.core.model.Actor;
import org.openlca.core.model.descriptors.ActorDescriptor;

public class ActorDao extends CategorizedEntityDao<Actor, ActorDescriptor> {

	public ActorDao(IDatabase database) {
		super(Actor.class, ActorDescriptor.class, database);
	}

}
