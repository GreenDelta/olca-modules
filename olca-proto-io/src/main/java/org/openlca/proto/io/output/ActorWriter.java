package org.openlca.proto.io.output;

import org.openlca.core.model.Actor;
import org.openlca.proto.EntityType;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class ActorWriter {

  private final WriterConfig config;

  public ActorWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Actor write(Actor a) {
    var proto = Proto.Actor.newBuilder();
    if (a == null)
      return proto.build();
    proto.setEntityType(EntityType.Actor);
    Out.map(a, proto);
    Out.dep(config, a.category);

    proto.setAddress(Strings.orEmpty(a.address));
    proto.setCity(Strings.orEmpty(a.city));
    proto.setCountry(Strings.orEmpty(a.country));
    proto.setEmail(Strings.orEmpty(a.email));
    proto.setTelefax(Strings.orEmpty(a.telefax));
    proto.setTelephone(Strings.orEmpty(a.telephone));
    proto.setWebsite(Strings.orEmpty(a.website));
    proto.setZipCode(Strings.orEmpty(a.zipCode));
    return proto.build();
  }
}
