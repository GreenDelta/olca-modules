
package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.input.ParameterRedefs;
import org.openlca.jsonld.input.Quantity;
import org.openlca.proto.ProtoProductSystem;

public class ProductSystemReader
	implements EntityReader<ProductSystem, ProtoProductSystem> {

	private final EntityResolver resolver;
	private final Map<String, Descriptor> processes = new HashMap<>();

	public ProductSystemReader(EntityResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public ProductSystem read(ProtoProductSystem proto) {
		var system = new ProductSystem();
		update(system, proto);
		return system;
	}

	@Override
	public void update(ProductSystem system, ProtoProductSystem proto) {

		// clear resources in case the reader is re-used
		processes.clear();

		Util.mapBase(system, ProtoWrap.of(proto), resolver);
		mapQRef(system, proto);
		addParameterSets(system, proto);
	}

	private void mapQRef(ProductSystem system, ProtoProductSystem proto) {
		system.targetAmount = proto.getTargetAmount();
		system.referenceProcess = Util.getProcess(resolver, proto.getRefProcess());

		Runnable clearQRef = () -> {
			system.referenceExchange = null;
			system.targetFlowPropertyFactor = null;
			system.targetUnit = null;
		};

		if (system.referenceProcess == null) {
			clearQRef.run();
			return;
		}

		var exchangeId = proto.getRefExchange().getInternalId();
		var qRef = system.referenceProcess.exchanges.stream()
			.filter(e -> e.internalId == exchangeId)
			.findAny()
			.orElse(null);
		if (qRef == null || qRef.flow == null) {
			clearQRef.run();
			return;
		}

		system.referenceExchange = qRef;
		var quantity = Quantity.of(qRef.flow)
			.withProperty(proto.getTargetFlowProperty())
			.withUnit(proto.getTargetUnit())
			.get();
		system.targetFlowPropertyFactor = quantity.factor();
		system.targetUnit = quantity.unit();
	}

	private void addParameterSets(ProductSystem sys, ProtoProductSystem proto) {
		sys.parameterSets.clear();
		for (int i = 0; i < proto.getParameterSetsCount(); i++) {
			var protoSet = proto.getParameterSets(i);
			var set = new ParameterRedefSet();
			set.name = protoSet.getName();
			set.description = protoSet.getDescription();
			set.isBaseline = protoSet.getIsBaseline();
			var redefs = Util.parameterRedefsOf(
				protoSet.getParametersList(), resolver);
			set.parameters.addAll(redefs);
			sys.parameterSets.add(set);
		}
	}
}
