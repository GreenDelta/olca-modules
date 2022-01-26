package org.openlca.io.ecospold2.output;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.model.Exchange;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spold2.Compartment;
import spold2.ElementaryExchange;

class ElemFlowMap {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<String, FlowMapEntry> flowMap;

	public ElemFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap == null
			? Collections.emptyMap()
			: flowMap.index();
	}

	public ElementaryExchange apply(Exchange olca) {
		if (olca == null || olca.flow == null) {
			log.warn("could not map exchange {}, exchange or flow is null",
				olca);
			return null;
		}
		var record = flowMap.get(olca.flow.refId);
		if (!isValid(record, olca)) {
			log.warn(
				"elementary flow {} cannot be mapped to an ecoinvent flow",
				olca.flow);
			return null;
		}
		return map(olca, record);
	}

	private boolean isValid(FlowMapEntry mapping, Exchange exchange) {
		if (mapping == null
			|| mapping.sourceFlow() == null
			|| mapping.targetFlow() == null)
			return false;
		var source = mapping.sourceFlow();
		var target = mapping.targetFlow();
		if (source.property == null
			|| source.unit == null
			|| target.flow == null
			|| target.unit == null)
			return false;
		return exchange != null
			&& exchange.flowPropertyFactor != null
			&& exchange.flowPropertyFactor.flowProperty != null
			&& Objects.equals(
			source.property.refId,
			exchange.flowPropertyFactor.flowProperty.refId)
			&& exchange.unit != null
			&& Objects.equals(source.unit.refId, exchange.unit.refId);
	}

	private ElementaryExchange map(Exchange olca, FlowMapEntry mapping) {
		var exchange = new ElementaryExchange();
		if (olca.isInput) {
			exchange.inputGroup = 4;
		} else {
			exchange.outputGroup = 4;
		}

		var target = mapping.targetFlow();
		exchange.id = new UUID(olca.id, 0L).toString();
		exchange.flowId = target.flow.refId;
		exchange.name = Strings.cut(target.flow.name, 120);
		exchange.compartment = createCompartment(target.flowCategory);
		exchange.unit = target.unit.name;
		exchange.unitId = target.unit.refId;
		exchange.amount = mapping.factor() * olca.amount;
		if (olca.formula != null) {
			exchange.mathematicalRelation = mapping.factor() + " * ("
				+ olca.formula + ")";
		}
		// TODO: convert uncertainty information
		return exchange;
	}

	private Compartment createCompartment(String path) {
		var comp = new Compartment();
		if (Strings.nullOrEmpty(path))
			return comp;
		comp.id = compartmentIdOf(path);
		var parts = path.split("/");
		if (parts.length > 0) {
			comp.compartment = parts[0];
		}
		if (parts.length > 1) {
			comp.subCompartment = parts[1];
		}
		return comp;
	}

	private String compartmentIdOf(String path) {
		if (Strings.nullOrEmpty(path))
			return null;
		// note this was generated from some old mappings and probably
		// is not up-to-date or does not contain every option
		return switch (path) {
			case "water/surface water" -> "963f8022-3e2e-4be9-ad4d-b3b7a2282099";
			case "water/ground-" -> "a119c440-7e83-4655-a874-97fe1468315a";
			case "water/ground-, long-term" -> "aa4362e0-b20a-448b-b2a0-261f4510deb5";
			case "natural resource/in ground" -> "6a098164-9f04-4f65-8104-ffab7f2677f3";
			case "air/non-urban air or from high stacks" -> "be7e06e9-0bf5-462e-99dc-fe4aee383c48";
			case "air/lower stratosphere + upper troposphere" -> "f335ce0e-b830-475a-adab-03858d9cbdaf";
			case "natural resource/land" -> "7d704b6f-d455-4f41-9c28-50b4f372f315";
			case "water/ocean" -> "65f8d2a1-63ed-479c-b86c-3bcf38e86320";
			case "soil/forestry" -> "15f47463-77ea-40d0-bfe8-ca632819f556";
			case "soil/agricultural" -> "e1bc9a16-5b6a-494f-98ef-49f461b1a11e";
			case "air/low population density, long-term" -> "23dbff79-8037-43e7-b270-5a3da416a284";
			case "air/unspecified" -> "7011f0aa-f5f9-4901-8c10-884ad8296812";
			case "air/urban air close to ground" -> "e8d7772c-55ca-4dd7-b605-fee5ae764578";
			case "water/unspecified" -> "e47f0a6c-3be8-4027-9eee-de251784f708";
			case "natural resource/biotic" -> "2d0acbd3-2083-4011-9a29-20c626b23dc3";
			case "soil/unspecified" -> "dbeb0ac7-0dec-439e-887a-9924cc8005dd";
			case "soil/industrial" -> "912f1ae3-734e-4cc6-bbf7-0f36843cd7de";
			case "natural resource/in water" -> "30347aef-a90b-46ba-8746-b53741aa779d";
			case "natural resource/in air" -> "45bb416c-a63b-429f-8754-b3f76a069c43";
			default -> null;
		};
	}
}
