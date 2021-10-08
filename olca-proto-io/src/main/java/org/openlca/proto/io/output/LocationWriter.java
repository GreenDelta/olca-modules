package org.openlca.proto.io.output;

import com.google.protobuf.ByteString;
import org.openlca.core.model.Location;
import org.openlca.proto.ProtoLocation;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class LocationWriter {

  private final WriterConfig config;

  public LocationWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoLocation write(Location location) {
    var proto = ProtoLocation.newBuilder();
    if (location == null)
      return proto.build();
    proto.setType(ProtoType.Location);
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
