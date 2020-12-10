package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class ProductSystemWriter {

  private final WriterConfig config;

  public ProductSystemWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.ProductSystem write(ProductSystem system) {
    var proto = Proto.ProductSystem.newBuilder();
    if (system == null)
      return proto.build();

    // root entity fields
    proto.setType("ProductSystem");
    proto.setId(Strings.orEmpty(system.refId));
    proto.setName(Strings.orEmpty(system.name));
    proto.setDescription(Strings.orEmpty(system.description));
    proto.setVersion(Version.asString(system.version));
    if (system.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(system.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(system.tags)) {
      Arrays.stream(system.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (system.category != null) {
      proto.setCategory(Out.refOf(system.category, config));
    }

    // model specific fields
    // TODO

    return proto.build();
  }
}
