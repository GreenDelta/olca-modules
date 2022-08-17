
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Location;
import org.openlca.proto.ProtoLocation;

public record LocationReader(EntityResolver resolver)
	implements EntityReader<Location, ProtoLocation> {

	@Override
	public Location read(ProtoLocation proto) {
		var location = new Location();
		update(location, proto);
		return location;
	}

	@Override
	public void update(Location location, ProtoLocation proto) {
		Util.mapBase(location, ProtoWrap.of(proto), resolver);
		location.code = proto.getCode();
		location.latitude = proto.getLatitude();
		location.longitude = proto.getLongitude();
		var geom = proto.getGeometryBytes();
		if (!geom.isEmpty()) {
			location.geodata = geom.toByteArray();
		}
	}
}
