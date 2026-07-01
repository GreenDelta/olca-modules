package org.openlca.io.ecospold1.output;

import java.util.Map;
import java.util.Optional;

import org.openlca.commons.Strings;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.io.maps.FlowRef;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactFactor;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IExchange;

class ExportFlow {

	private final Flow flow;
	private final FlowRef mapping;
	private final double factor;
	private final IExchange exchange;

	private ExportFlow(
		Flow flow, FlowRef mapping, double factor, IExchange exchange
	) {
		this.flow = flow;
		this.mapping = mapping;
		this.factor = factor;
		this.exchange = exchange;
	}

	static Optional<IExchange> of(
		ImpactFactor f, Map<String, FlowMapEntry> flowMap, DataSet ds
	) {
		if (f == null || f.flow == null || ds == null)
			return Optional.empty();

		var e = ds.withExchange();
		e.setNumber((int) f.id);

		var mapping = flowMap != null
			? flowMap.get(f.flow.refId)
			: null;
		double factor = mapping != null
			? mapping.factor()
			: 1.0;

		var mappingTarget = mapping != null
			? mapping.targetFlow()
			: null;

		new ExportFlow(f.flow, mappingTarget, factor, e).fill();

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
}
