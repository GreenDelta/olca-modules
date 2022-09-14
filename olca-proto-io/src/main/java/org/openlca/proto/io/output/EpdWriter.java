package org.openlca.proto.io.output;

import org.openlca.core.model.Epd;
import org.openlca.proto.ProtoEpd;
import org.openlca.proto.ProtoEpdModule;
import org.openlca.proto.ProtoEpdProduct;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

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

		proto.setUrn(Strings.orEmpty(epd.urn));
		config.dep(epd.manufacturer, proto::setManufacturer);
		config.dep(epd.verifier, proto::setVerifier);
		config.dep(epd.programOperator, proto::setProgramOperator);
		config.dep(epd.pcr, proto::setPcr);

		if (epd.product != null) {
			var prodProto = ProtoEpdProduct.newBuilder();
			config.dep(epd.product.flow, prodProto::setFlow);
			config.dep(epd.product.property, prodProto::setFlowProperty);
			config.dep(epd.product.unit, prodProto::setUnit);
			prodProto.setAmount(epd.product.amount);
			proto.setProduct(prodProto);
		}

		for (var mod : epd.modules) {
			var protoMod = ProtoEpdModule.newBuilder()
				.setName(Strings.orEmpty(mod.name))
				.setMultiplier(mod.multiplier);
			config.dep(mod.result, protoMod::setResult);
			proto.addModules(protoMod);
		}

		return proto.build();
	}
}
