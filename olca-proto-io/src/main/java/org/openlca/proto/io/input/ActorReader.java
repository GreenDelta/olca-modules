package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.proto.ProtoActor;

public record ActorReader(EntityResolver resolver)
	implements EntityReader<Actor, ProtoActor> {

	@Override
	public Actor read(ProtoActor proto) {
		var actor = new Actor();
		update(actor, proto);
		return actor;
	}

	@Override
	public void update(Actor actor, ProtoActor proto) {
		Util.mapBase(actor, ProtoWrap.of(proto), resolver);
		actor.address = proto.getAddress();
		actor.city = proto.getCity();
		actor.country = proto.getCountry();
		actor.email = proto.getEmail();
		actor.telefax = proto.getTelefax();
		actor.telephone = proto.getTelephone();
		actor.website = proto.getWebsite();
		actor.zipCode = proto.getZipCode();
	}

}
