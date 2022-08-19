
package org.openlca.proto.io.input;

import java.util.HashMap;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.proto.ProtoFlow;

public record FlowReader(EntityResolver resolver)
	implements EntityReader<Flow, ProtoFlow> {

	@Override
	public Flow read(ProtoFlow proto) {
		var flow = new Flow();
		update(flow, proto);
		return flow;
	}

	@Override
	public void update(Flow flow, ProtoFlow proto) {
		Util.mapBase(flow, ProtoBox.of(proto), resolver);
		flow.flowType = switch (proto.getFlowType()) {
			case ELEMENTARY_FLOW -> FlowType.ELEMENTARY_FLOW;
			case PRODUCT_FLOW -> FlowType.PRODUCT_FLOW;
			case WASTE_FLOW -> FlowType.WASTE_FLOW;
			default -> null;
		};
		flow.casNumber = proto.getCas();
		flow.synonyms = proto.getSynonyms();
		flow.formula = proto.getFormula();
		flow.infrastructureFlow = proto.getIsInfrastructureFlow();
		flow.location = Util.getLocation(resolver, proto.getLocation());
		mapPropertyFactors(flow, proto);
	}

	private void mapPropertyFactors(Flow flow, ProtoFlow proto) {

		// sync with existing flow property factors. we identify
		// them by their flow property ID.
		var oldFactors = new HashMap<String, FlowPropertyFactor>(
			flow.flowPropertyFactors.size());
		for (var factor : flow.flowPropertyFactors) {
			if (factor.flowProperty == null)
				continue;
			oldFactors.put(factor.flowProperty.refId, factor);
		}
		flow.flowPropertyFactors.clear();

		for (int i = 0; i < proto.getFlowPropertiesCount(); i++) {
			var protoProp = proto.getFlowProperties(i);
			var property = Util.getFlowProperty(resolver, protoProp.getFlowProperty());
			if (property == null)
				continue;

			// create or update the factor
			var factor = oldFactors.getOrDefault(
				property.refId, new FlowPropertyFactor());
			factor.flowProperty = property;
			factor.conversionFactor = protoProp.getConversionFactor();
			flow.flowPropertyFactors.add(factor);

			// check if it is the reference flow property
			if (protoProp.getIsRefFlowProperty()) {
				flow.referenceFlowProperty = property;
			}
		}
	}
}
