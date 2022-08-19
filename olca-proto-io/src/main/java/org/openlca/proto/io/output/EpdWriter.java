package org.openlca.proto.io.output;

import org.openlca.core.model.Epd;
import org.openlca.proto.ProtoEpd;
import org.openlca.proto.ProtoType;

public class EpdWriter {

	private final WriterConfig config;

	public EpdWriter(WriterConfig config) {
		this.config = config;
	}

	public ProtoEpd write(Epd epd) {
		var proto = ProtoEpd.newBuilder();
		if (epd == null)
			return proto.build();
		proto.setType(ProtoType.Epd);
		Out.map(epd, proto);

		// TODO: fill EPD fields

		return proto.build();
	}
}
