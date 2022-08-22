
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.EpdProduct;
import org.openlca.proto.ProtoEpd;
import org.openlca.proto.ProtoEpdProduct;

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
		Util.mapBase(epd, ProtoBox.of(proto), resolver);
		epd.urn = proto.getUrn();
		epd.manufacturer = Util.getActor(resolver, proto.getManufacturer());
		epd.verifier = Util.getActor(resolver, proto.getVerifier());
		epd.programOperator = Util.getActor(resolver, proto.getProgramOperator());
		epd.pcr = Util.getSource(resolver, proto.getPcr());
		epd.product = product(proto.getProduct());
		mapModules(epd, proto);
	}


	private EpdProduct product(ProtoEpdProduct proto) {
		var flow = Util.getFlow(resolver, proto.getFlow());
		if (flow == null)
			return null;
		var quantity = Quantity.of(flow)
			.withProperty(proto.getFlowProperty())
			.withUnit(proto.getUnit())
			.get();
		var product = new EpdProduct();
		product.flow = flow;
		product.property = quantity.property();
		product.unit = quantity.unit();
		product.amount = proto.getAmount();
		return product;
	}

	private void mapModules(Epd epd, ProtoEpd proto) {
		epd.modules.clear();
		for (int i = 0; i < proto.getModulesCount(); i++) {
			var protoMod = proto.getModules(i);
			var module = new EpdModule();
			module.name = protoMod.getName();
			module.multiplier = protoMod.getMultiplier();
			module.result = Util.getResult(resolver, protoMod.getResult());
			epd.modules.add(module);
		}
	}

}
