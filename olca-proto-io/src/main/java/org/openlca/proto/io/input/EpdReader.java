
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Epd;
import org.openlca.proto.ProtoEpd;

public record EpdReader(EntityResolver resolver)
	implements EntityReader<Epd, ProtoEpd> {

	@Override
	public Epd read(ProtoEpd proto) {
		var epd = new Epd();
		update(epd, proto);
		return epd;
	}

	@Override
	public void update(Epd epd, ProtoEpd proto) {
		Util.mapBase(epd, ProtoWrap.of(proto), resolver);

	}
}
