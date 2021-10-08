package org.openlca.proto.io.input;

import org.openlca.core.database.ActorDao;
import org.openlca.core.model.Actor;
import org.openlca.proto.ProtoActor;

class ActorImport implements Import<Actor> {

  private final ProtoImport imp;

  ActorImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Actor> of(String id) {
    var actor = imp.get(Actor.class, id);

    // check if we are in update mode
    var update = false;
    if (actor != null) {
      update = imp.shouldUpdate(actor);
      if (!update)
        return ImportStatus.skipped(actor);
    }

    // resolve the proto object
    var proto = imp.reader.getActor(id);
    if (proto == null)
      return actor != null
        ? ImportStatus.skipped(actor)
        : ImportStatus.error("Could not resolve Actor " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(actor, wrap))
        return ImportStatus.skipped(actor);
    }

    // map the data
    if (actor == null) {
      actor = new Actor();
    }
    wrap.mapTo(actor, imp);
    map(proto, actor);

    // insert or update it
    var dao = new ActorDao(imp.db);
    actor = update
      ? dao.update(actor)
      : dao.insert(actor);
    imp.putHandled(actor);
    return update
      ? ImportStatus.updated(actor)
      : ImportStatus.created(actor);
  }

  private void map(ProtoActor proto, Actor actor) {
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
