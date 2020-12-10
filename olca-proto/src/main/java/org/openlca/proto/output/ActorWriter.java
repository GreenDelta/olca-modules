package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
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

    // root entity fields
    proto.setType("Actor");
    proto.setId(Strings.orEmpty(a.refId));
    proto.setName(Strings.orEmpty(a.name));
    proto.setDescription(Strings.orEmpty(a.description));
    proto.setVersion(Version.asString(a.version));
    if (a.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(a.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(a.tags)) {
      Arrays.stream(a.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (a.category != null) {
      proto.setCategory(Out.refOf(a.category, config));
    }

    // model specific fields
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
