package org.openlca.proto.io.input;

import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.proto.ProtoLocation;

class LocationImport implements Import<Location> {

  private final ProtoImport imp;

  LocationImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Location> of(String id) {
    var location = imp.get(Location.class, id);

    // check if we are in update mode
    var update = false;
    if (location != null) {
      update = imp.shouldUpdate(location);
      if(!update) {
        return ImportStatus.skipped(location);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getLocation(id);
    if (proto == null)
      return location != null
        ? ImportStatus.skipped(location)
        : ImportStatus.error("Could not resolve Location " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(location, wrap))
        return ImportStatus.skipped(location);
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
    return update
      ? ImportStatus.updated(location)
      : ImportStatus.created(location);
  }

  private void map(ProtoLocation proto, Location location) {
    location.code = proto.getCode();
    location.latitude = proto.getLatitude();
    location.longitude = proto.getLongitude();
    var geom = proto.getGeometryBytes();
    if (!geom.isEmpty()) {
      location.geodata = geom.toByteArray();
    }
  }
}
