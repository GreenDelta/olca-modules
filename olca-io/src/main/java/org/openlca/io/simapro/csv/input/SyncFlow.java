package org.openlca.io.simapro.csv.input;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openlca.core.model.Flow;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

record SyncFlow(
	Flow flow,
	boolean isMapped,
	double mapFactor) {

	static SyncFlow of(Flow flow) {
		return new SyncFlow(flow, false, 1.0);
	}

	static SyncFlow ofMapped(Flow flow, double mapFactor) {
		return new SyncFlow(flow, true, mapFactor);
	}

	static SyncFlow empty() {
		return new SyncFlow(null, false, 0);
	}

	boolean isEmpty() {
		return flow == null;
	}

	static String mappingKeyOf(
		Compartment compartment, String name, String unit) {

		var top = compartment.type() != null
			? compartment.type().exchangeHeader()
			: "";
		var sub = compartment.sub() != null
			? compartment.sub().toString()
			: SubCompartment.UNSPECIFIED.toString();
		return Stream.of(top, sub, name, unit)
			.map(Strings::orEmpty)
			.map(s -> s.trim().toLowerCase())
			.collect(Collectors.joining("/"));
	}

	static String refIdOf(
		Compartment compartment, String name, UnitMappingEntry unit) {
		var groupId = unit.unitGroup != null
			? unit.unitGroup.refId
			: "";
		var path = mappingKeyOf(compartment, name, groupId);
		return KeyGen.get(path);
	}

}
