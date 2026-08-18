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
class ExchangeBuilder {

	private final DataSet ds;
	private final Map<String, FlowMapEntry> flowMap;
	private final @Nullable Process owner;
	private final @Nullable FlowNameFormatter names;

	private ExchangeBuilder(
		DataSet ds,
		Map<String, FlowMapEntry> flowMap,
		@Nullable Process owner,
		@Nullable FlowNameFormatter names) {
		this.ds = ds;
		this.flowMap = flowMap;
		this.owner = owner;
		this.names = names;
	}

	static ExchangeBuilder of(
		DataSet ds,
		Map<String, FlowMapEntry> flowMap,
		Process owner,
		FlowNameFormatter names
	) {
		return new ExchangeBuilder(ds, flowMap, owner, names);
	}

	static ExchangeBuilder of(DataSet ds, Map<String, FlowMapEntry> flowMap) {
		return new ExchangeBuilder(ds, flowMap, null, null);
	}

	@Nullable
	IExchange create(Exchange exchange) {
		var quant = FlowQuant.of(exchange, flowMap);
		return quant != null
			? fill(ds.withExchange(), quant, exchange)
			: null;
	}

	void create(ImpactFactor factor) {
		var quant = FlowQuant.of(factor, flowMap);
		if (quant != null) {
			fill(ds.withExchange(), quant, null);
		}
	}

	private IExchange fill(
		IExchange exchange, FlowQuant quant, @Nullable Exchange origin
	) {
		exchange.setNumber(quant.number());
		exchange.setName(name(quant, origin));
		exchange.setMeanValue(amount(quant));
		exchange.setUnit(unit(quant));
		fillCategory(quant, exchange);
		fillFlowAttributes(quant, exchange);
		fillUncertainty(quant, exchange);
		return exchange;
	}

	private String name(FlowQuant quant, @Nullable Exchange origin) {
		var m = quant.mapping();
		if (m != null
			&& m.flow != null
			&& Strings.isNotBlank(m.flow.name)) {
			return m.flow.name;
		}
		return names != null && owner != null
			? names.of(owner, origin)
			: quant.flow().name;
	}

	private String unit(FlowQuant quant) {
		var m = quant.mapping();
		if (m != null
			&& m.unit != null
			&& Strings.isNotBlank(m.unit.name)) {
			return m.unit.name;
		}
		return quant.unit().name;
	}

	private double amount(FlowQuant quant) {
		return quant.mapping() != null
			? quant.factor() * quant.amount()
			: quant.amount();
	}

	private void fillCategory(FlowQuant quant, IExchange exchange) {
		var m = quant.mapping();
		if (m != null && Strings.isNotBlank(m.flowCategory)) {
			Categories.map(m.flowCategory, exchange);
		} else {
			Categories.map(quant.flow().category, exchange);
		}
	}

	private void fillFlowAttributes(FlowQuant quant, IExchange exchange) {
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

	private void fillUncertainty(FlowQuant quant, IExchange e) {
		var u = quant.uncertainty();
		if (u == null
			|| u.distributionType == null
			|| u.parameter1 == null
			|| u.parameter2 == null)
			return;
		if (u.distributionType == UncertaintyType.TRIANGLE
			&& u.parameter3 == null)
			return;

		switch (u.distributionType) {

			case NORMAL -> {
				e.setMeanValue(conv(u.parameter1, quant));
				e.setStandardDeviation95(conv(u.parameter2 * 2, quant));
				e.setUncertaintyType(2);
			}

			case LOG_NORMAL -> {
				e.setMeanValue(conv(u.parameter1, quant));
				e.setStandardDeviation95(Math.pow(u.parameter2, 2));
				e.setUncertaintyType(1);
			}

			case TRIANGLE -> {
				e.setMinValue(conv(u.parameter1, quant));
				e.setMostLikelyValue(conv(u.parameter2, quant));
				e.setMaxValue(conv(u.parameter3, quant));
				e.setUncertaintyType(3);
			}

			case UNIFORM -> {
				e.setMinValue(conv(u.parameter1, quant));
				e.setMaxValue(conv(u.parameter2, quant));
				e.setUncertaintyType(4);
			}
			default -> {
			}
		}
	}

	private double conv(@Nullable Double v, FlowQuant quant) {
		if (v == null) return 0;
		return quant.mapping() != null
			? v * quant.factor()
			: v;
	}
}
