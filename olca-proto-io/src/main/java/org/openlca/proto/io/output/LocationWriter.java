package org.openlca.proto.io.output;

import org.openlca.commons.Strings;
import org.openlca.core.model.Location;
import org.openlca.proto.ProtoLocation;
import org.openlca.proto.ProtoType;

import com.google.protobuf.ByteString;

public class LocationWriter {

  public ProtoLocation write(Location location) {
    var proto = ProtoLocation.newBuilder();
    if (location == null)
      return proto.build();
    proto.setType(ProtoType.Location);
    Out.map(location, proto);

    proto.setCode(Strings.notNull(location.code));
    proto.setLatitude(location.latitude);
    proto.setLongitude(location.longitude);
    if (location.geodata != null) {
      proto.setGeometryBytes(
        ByteString.copyFrom(location.geodata));
    }

    return proto.build();
  }
}
