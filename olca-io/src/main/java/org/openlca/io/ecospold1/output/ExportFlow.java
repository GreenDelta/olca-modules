package org.openlca.io.ecospold1.output;

import java.util.Map;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Strings;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.UncertaintyType;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IExchange;

@NullMarked
class ExportFlow {

	private final IExchange exchange;
	private final FlowQuant quant;

	@Nullable
	private final Exchange value;

	@Nullable
	private final Process owner;

	@Nullable
	private final FlowNameFormatter flowNames;

	private ExportFlow(
		IExchange exchange,
		FlowQuant quant,
		@Nullable Exchange value,
		@Nullable Process owner,
		@Nullable FlowNameFormatter flowNames) {
		this.exchange = exchange;
		this.quant = quant;
		this.value = value;
		this.owner = owner;
		this.flowNames = flowNames;
	}

	static void of(
		ImpactFactor factor, DataSet ds, Map<String, FlowMapEntry> mappings
	) {
		var quant = FlowQuant.of(factor, mappings);
		if (quant != null) {
			new ExportFlow(ds.withExchange(), quant, null, null, null).fill();
		}
	}

	@Nullable
	static IExchange of(
		Exchange exchange,
		DataSet ds,
		Map<String, FlowMapEntry> mappings,
		Process owner,
		FlowNameFormatter flowNames
	) {
		var quant = FlowQuant.of(exchange, mappings);
		return quant != null
			? new ExportFlow(
				ds.withExchange(), quant, exchange, owner, flowNames).fill()
			: null;
	}

	private IExchange fill() {
		exchange.setNumber(quant.number());
		exchange.setName(name());
		exchange.setMeanValue(amount());
		exchange.setUnit(unit());
		fillCategory();
		fillFlowAttributes();
		fillUncertainty();
		return exchange;
	}

	private String name() {
		var m = quant.mapping();
		if (m != null
			&& m.flow != null
			&& Strings.isNotBlank(m.flow.name)) {
			return m.flow.name;
		}
		return flowNames != null && owner != null
			? flowNames.of(owner, value)
			: quant.flow().name;
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
		if (Strings.isNotBlank(flow.formula)) {
			exchange.setFormula(flow.formula);
		}

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

	private void fillUncertainty() {
		var u = quant.uncertainty();
		if (u == null
			|| u.distributionType == null
			|| u.parameter1 == null
			|| u.parameter2 == null)
			return;
		if (u.distributionType == UncertaintyType.TRIANGLE
			&& u.parameter3 == null)
			return;

		var e = exchange;

		switch (u.distributionType) {

			case NORMAL -> {
				e.setMeanValue(conv(u.parameter1));
				e.setStandardDeviation95(conv(u.parameter2 * 2));
				e.setUncertaintyType(2);
			}

			case LOG_NORMAL -> {
				e.setMeanValue(conv(u.parameter1));
				e.setStandardDeviation95(Math.pow(u.parameter2, 2));
				e.setUncertaintyType(1);
			}

			case TRIANGLE -> {
				e.setMinValue(conv(u.parameter1));
				e.setMostLikelyValue(conv(u.parameter2));
				e.setMaxValue(conv(u.parameter3));
				e.setUncertaintyType(3);
			}

			case UNIFORM -> {
				e.setMinValue(conv(u.parameter1));
				e.setMaxValue(conv(u.parameter2));
				e.setUncertaintyType(4);
			}
			default -> {
			}
		}
	}

	private double conv(@Nullable Double v) {
		if (v == null) return 0;
		return quant.mapping() != null
			? v * quant.factor()
			: v;
	}
}
