package org.openlca.proto.input;

import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.proto.Proto;

public class LocationImport {

  private final ProtoImport imp;

  public LocationImport(ProtoImport imp) {
    this.imp = imp;
  }

  public Location of(String id) {
    if (id == null)
      return null;
    var location = imp.get(Location.class, id);

    // check if we are in update mode
    var update = false;
    if (location != null) {
      update = imp.shouldUpdate(location);
      if(!update) {
        return location;
      }
    }

    // check the proto object
    var proto = imp.store.getLocation(id);
    if (proto == null)
      return location;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(location, wrap))
        return location;
    }

    // map the data
    if (location == null) {
      location = new Location();
      location.refId = id;
    }
    wrap.mapTo(location, imp);
    map(proto, location);

    // insert it
    var dao = new LocationDao(imp.db);
    location = update
      ? dao.update(location)
      : dao.insert(location);
    imp.putHandled(location);
    return location;
  }

  private void map(Proto.Location proto, Location location) {
    location.code = proto.getCode();
    location.latitude = proto.getLatitude();
    location.longitude = proto.getLongitude();
    var geom = proto.getGeometryBytes();
    if (!geom.isEmpty()) {
      location.geodata = geom.toByteArray();
    }
  }
}
