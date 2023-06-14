package org.openlca.core.database.descriptors;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.ActorDescriptor;

public class ActorDescriptors extends RootDescriptorReader<ActorDescriptor> {

	private ActorDescriptors(IDatabase db) {
		super(new ActorDao(db));
	}

	public static ActorDescriptors of(IDatabase db) {
		return new ActorDescriptors(db);
	}
}
