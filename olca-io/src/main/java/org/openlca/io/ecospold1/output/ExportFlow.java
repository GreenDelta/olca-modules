package org.openlca.io.ecospold1.output;

import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Strings;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactFactor;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IExchange;

class ExportFlow {

	private final IExchange exchange;
	private final FlowQuant quant;

	private ExportFlow(
		@NonNull IExchange exchange, @NonNull FlowQuant quant) {
		this.exchange = exchange;
		this.quant = quant;
	}

	@Nullable
	static IExchange of(
		ImpactFactor factor, DataSet ds, Map<String, FlowMapEntry> mappings
	) {
		var quant = FlowQuant.of(factor, mappings);
		return quant != null
			? new ExportFlow(ds.withExchange(), quant).fill()
			: null;
	}

	@Nullable
	static IExchange of(
		Exchange exchange, DataSet ds, Map<String, FlowMapEntry> mappings
	) {
		var quant = FlowQuant.of(exchange, mappings);
		return quant != null
			? new ExportFlow(ds.withExchange(), quant).fill()
			: null;
	}

	private IExchange fill() {
		exchange.setName(name());
		exchange.setMeanValue(amount());
		exchange.setUnit(unit());
		fillCategory();
		fillFlowAttributes();
		return exchange;
	}

	private String name() {
		var m = quant.mapping();
		if (m != null
			&& m.flow != null
			&& Strings.isNotBlank(m.flow.name)) {
			return m.flow.name;
		}
		return quant.flow().name;
	}

	private String unit() {
		var m = quant.mapping();
		if (m != null
			&& m.unit != null
			&& Strings.isNotBlank(m.unit.name)) {
			return m.unit.name;
		}
		return quant.unit().name;
	}

	private double amount() {
		return quant.mapping() != null
			? quant.factor() * quant.amount()
			: quant.amount();
	}

	private void fillCategory() {
		var m = quant.mapping();
		if (m != null && Strings.isNotBlank(m.flowCategory)) {
			Categories.map(m.flowCategory, exchange);
		} else {
			Categories.map(quant.flow().category, exchange);
		}
	}

	private void fillFlowAttributes() {
		var flow = quant.flow();
		if (Strings.isNotBlank(flow.casNumber)) {
			exchange.setCASNumber(flow.casNumber);
		}
		exchange.setFormula(flow.formula);

		if (flow.flowType == FlowType.ELEMENTARY_FLOW)
			return;

		if (flow.infrastructureFlow) {
			// only set it, if it is explicitly true
			exchange.setInfrastructureProcess(true);
		}

		if (flow.location != null) {
			var code = Strings.isNotBlank(flow.location.code)
				? flow.location.code
				: flow.location.name;
			exchange.setLocation(code);
		}
	}

}
