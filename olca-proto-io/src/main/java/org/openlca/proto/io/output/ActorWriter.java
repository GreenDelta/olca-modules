package org.openlca.proto.io.output;

import org.openlca.core.model.Actor;
import org.openlca.proto.ProtoActor;
import org.openlca.proto.ProtoType;
import org.openlca.commons.Strings;

public class ActorWriter {

  public ProtoActor write(Actor a) {
    var proto = ProtoActor.newBuilder();
    if (a == null)
      return proto.build();
    proto.setType(ProtoType.Actor);
    Out.map(a, proto);

    proto.setAddress(Strings.notNull(a.address));
    proto.setCity(Strings.notNull(a.city));
    proto.setCountry(Strings.notNull(a.country));
    proto.setEmail(Strings.notNull(a.email));
    proto.setTelefax(Strings.notNull(a.telefax));
    proto.setTelephone(Strings.notNull(a.telephone));
    proto.setWebsite(Strings.notNull(a.website));
    proto.setZipCode(Strings.notNull(a.zipCode));
    return proto.build();
  }
}
