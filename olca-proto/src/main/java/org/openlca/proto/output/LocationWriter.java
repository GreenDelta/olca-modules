package org.openlca.proto.output;

import com.google.protobuf.ByteString;
import org.openlca.core.model.Location;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.generated.Proto;
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
    proto.setEntityType(EntityType.Location);
    Out.map(location, proto);
    Out.dep(config, location.category);

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
