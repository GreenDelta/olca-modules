package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import com.google.protobuf.ByteString;
import org.openlca.core.model.Location;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class LocationWriter {

  private final WriterConfig config;

  public LocationWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Location write(Location location) {
    var proto = Proto.Location.newBuilder();
    if (location == null)
      return proto.build();

    // root entity fields
    proto.setType("Location");
    proto.setId(Strings.orEmpty(location.refId));
    proto.setName(Strings.orEmpty(location.name));
    proto.setDescription(Strings.orEmpty(location.description));
    proto.setVersion(Version.asString(location.version));
    if (location.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(location.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(location.tags)) {
      Arrays.stream(location.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (location.category != null) {
      proto.setCategory(Out.refOf(location.category, config));
    }

    // model specific fields
    proto.setCode(Strings.orEmpty(location.code));
    proto.setLatitude(location.latitude);
    proto.setLongitude(location.longitude);
    if (location.geodata != null) {
      proto.setGeometryBytes(
        ByteString.copyFrom(location.geodata));
    }

    return proto.build();
  }
}
