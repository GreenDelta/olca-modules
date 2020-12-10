package org.openlca.proto.input;

import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.proto.Proto;

public class ActorImport {

  private final ProtoImport imp;

  public ActorImport(ProtoImport imp) {
    this.imp = imp;
  }

  public Actor of(String id) {
    if (id == null)
      return null;
    var actor = imp.get(Actor.class, id);

    // check if we are in update mode
    var update = false;
    if (actor != null) {
      update = imp.shouldUpdate(actor);
      if (!update)
        return actor;
    }

    // check the proto object
    var proto = imp.store.getActor(id);
    if (proto == null)
      return actor;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(actor, wrap))
        return actor;
    }

    // map the data
    if (actor == null) {
      actor = new Actor();
    }
    wrap.mapTo(actor, imp);
    map(proto, actor);

    // insert it
    var dao = new ActorDao(imp.db);
    actor = update
      ? dao.update(actor)
      : dao.insert(actor);
    imp.putHandled(actor);
    return actor;
  }

  private void map(Proto.Actor proto, Actor actor) {
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
