package org.openlca.io.simapro.csv.input;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openlca.core.model.Flow;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.simapro.csv.method.ImpactFactorRow;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
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
		ElementaryFlowType type, ElementaryExchangeRow row) {
		var sub = SubCompartment.of(row.subCompartment());
		if (sub == null) {
			sub = SubCompartment.UNSPECIFIED;
		}
		return Stream.of(
				type.exchangeHeader(),
				sub.toString(),
				row.name(),
				row.unit())
			.map(Strings::orEmpty)
			.map(s -> s.trim().toLowerCase())
			.collect(Collectors.joining("/"));
	}

	static String mappingKeyOf(ImpactFactorRow row) {
		var c = Compartment.of(row.compartment(), row.subCompartment())
			.orElse(null);
		if (c == null)
			return Stream.of(row.flow(), row.unit())
				.map(Strings::orEmpty)
				.map(s -> s.trim().toLowerCase())
				.collect(Collectors.joining("/"));
		return Stream.of(
				c.type().exchangeHeader(),
				c.sub().toString(),
				row.flow(),
				row.unit())
			.map(Strings::orEmpty)
			.map(s -> s.trim().toLowerCase())
			.collect(Collectors.joining("/"));
	}

}
