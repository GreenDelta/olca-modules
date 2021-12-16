package org.openlca.io.ilcd.input;

import org.openlca.core.model.Exchange;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.util.ExchangeExtension;
import org.openlca.io.maps.SyncFlow;
import org.openlca.util.Strings;

record MappedExchange(
	SyncFlow syncFlow,
	Exchange exchange,
	org.openlca.ilcd.processes.Exchange origin,
	ExchangeExtension extension,
	boolean hasExtensionError) {

	static MappedExchange of(SyncFlow syncFlow,
		org.openlca.ilcd.processes.Exchange origin) {

		var exchange = new Exchange();
		exchange.internalId = origin.id;
		exchange.isInput = origin.direction == ExchangeDirection.INPUT;
		exchange.flow = syncFlow.flow();
		exchange.unit = syncFlow.unit();
		exchange.flowPropertyFactor = syncFlow.property();

		// check if there is an extension that we can apply
		var ext = new ExchangeExtension(origin);
		if (syncFlow.isMapped() || !ext.isValid()) {
			ext = null;
		}
		var extError = ext != null && !apply(ext, exchange);
		if (extError) {
			ext = null;
		}

		if (ext == null) {
			var amount = origin.resultingAmount != null
				? origin.resultingAmount
				: origin.meanAmount;
			exchange.amount = syncFlow.isMapped()
				? syncFlow.mapFactor() * amount
				: amount;

			if (Strings.notEmpty(origin.variable)) {
				var formula = origin.variable + " * " + origin.meanAmount;
				exchange.formula = syncFlow.isMapped() && syncFlow.mapFactor() != 1
					? syncFlow.mapFactor() + " * " + formula
					: formula;
			}

			new UncertaintyConverter().map(origin, exchange);
			if (exchange.uncertainty != null && syncFlow.isMapped()) {
				exchange.uncertainty.scale(syncFlow.mapFactor());
			}
		}

		return new MappedExchange(syncFlow, exchange, origin, ext, extError);
	}

	private static boolean apply(ExchangeExtension ext, Exchange exchange) {
		var flow = exchange.flow;

		// set the unit and flow property from the extension attributes
		var factor = flow.flowPropertyFactors.stream()
			.filter(f -> f.flowProperty != null
				&& Strings.nullOrEqual(f.flowProperty.refId, ext.getPropertyId()))
			.findAny()
			.orElse(null);
		if (factor == null)
			return false;

		var group = factor.flowProperty.unitGroup;
		if (group == null)
			return false;

		var unit = group.units.stream()
			.filter(u -> Strings.nullOrEqual(u.refId, ext.getUnitId()))
			.findAny()
			.orElse(null);
		if (unit == null)
			return false;

		exchange.flowPropertyFactor = factor;
		exchange.unit = unit;

		exchange.dqEntry = ext.getPedigreeUncertainty();
		exchange.baseUncertainty = ext.getBaseUncertainty();
		exchange.amount = ext.getAmount();
		exchange.formula = ext.getFormula();
		if (ext.isAvoidedProduct()) {
			exchange.isInput = !exchange.isInput;
			exchange.isAvoided = true;
		}
		return true;
	}

	/**
	 * Get the ID of a possible provider, or null, if there is
	 * no provider defined.
	 */
	String providerId() {
		if (syncFlow.isMapped()) {
			var provider = syncFlow.provider();
			return provider != null
				? provider.refId
				: null;
		}
		return extension != null
			? extension.getDefaultProvider()
			: null;
	}
}
