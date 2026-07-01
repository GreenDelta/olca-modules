package org.openlca.io.ecospold1.output;

import java.util.Map;
import java.util.Optional;

import org.openlca.commons.Strings;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.io.maps.FlowRef;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Unit;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IExchange;

class ExportFlow {

	private final Flow flow;
	private final FlowRef mapping;
	private final double factor;
	private final IExchange exchange;

	private ExportFlow(Flow flow, IExchange e, MappingFactor mf) {
		this.flow = flow;
		this.exchange = e;
		this.mapping = mf != null ? mf.flowRef : null;
		this.factor = mf != null ? mf.value : 1.0;
	}

	static Optional<IExchange> of(
		ImpactFactor f, DataSet ds, Map<String, FlowMapEntry> mappings
	) {
		if (f == null || f.flow == null || ds == null)
			return Optional.empty();
		var e = ds.withExchange();
		e.setNumber((int) f.id);
		var mf = MappingFactor.of(f, mappings).orElse(null);
		new ExportFlow(f.flow, e, mf).fill();
		return Optional.of(e);
	}

	static Optional<IExchange> of(
		Exchange exchange, DataSet ds, Map<String, FlowMapEntry> mappings
	) {
		if (exchange == null || exchange.flow == null || ds == null)
			return Optional.empty();
		var e = ds.withExchange();
		e.setNumber((int) exchange.id);
		var mf = MappingFactor.of(exchange, mappings).orElse(null);
		new ExportFlow(exchange.flow, e, mf).fill();
		return Optional.of(e);
	}

	private void fill() {
		exchange.setName(name());
		fillCategory();
	}

	private void fillCategory() {
		if (mapping != null && Strings.isNotBlank(mapping.flowCategory)) {
			Categories.map(mapping.flowCategory, exchange);
		} else {
			Categories.map(flow.category, exchange);
		}
	}

	private String name() {
		if (mapping != null
			&& mapping.flow != null
			&& Strings.isNotBlank(mapping.flow.name)) {
			return mapping.flow.name;
		}
		return flow.name;
	}

	private String unit() {
		if (mapping != null
		&& mapping.unit != null
		&& Strings.isNotBlank(mapping.unit.name)) {
			return mapping.unit.name;
		}
		return
	}

	private record MappingFactor(FlowRef flowRef, double value) {

		static Optional<MappingFactor> of(
			ImpactFactor f, Map<String, FlowMapEntry> mappings
		) {
			if (f == null || f.flow == null || mappings == null)
				return Optional.empty();
			var entry = mappings.get(f.flow.refId);
			if (skip(entry))
				return Optional.empty();
			double factor = factorOf(entry, f.unit, f.flowPropertyFactor);
			if (factor == 0)
				return Optional.empty();
			return Optional.of(new MappingFactor(entry.targetFlow(), 1 / factor));
		}

		static Optional<MappingFactor> of(
			Exchange e, Map<String, FlowMapEntry> mappings
		) {
			if (e == null || e.flow == null || mappings == null)
				return Optional.empty();
			var entry = mappings.get(e.flow.refId);
			if (skip(entry))
				return Optional.empty();
			double factor = factorOf(entry, e.unit, e.flowPropertyFactor);
			return factor == 0
				? Optional.empty()
				: Optional.of(new MappingFactor(entry.targetFlow(), factor));
		}

		private static double factorOf(
			FlowMapEntry e, Unit unit, FlowPropertyFactor fpf
		) {
			double f = e.factor();

			// TODO: we need to check and may convert units here!
			return f;
		}

		private static boolean skip(FlowMapEntry e) {
			return e == null
				|| e.targetFlow() == null
				|| e.targetFlow().flow == null;
		}
	}
}
