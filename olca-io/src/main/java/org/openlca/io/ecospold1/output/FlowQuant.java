package org.openlca.io.ecospold1.output;

import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.io.maps.FlowRef;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Unit;

/// Process exchanges and characterization factors are flow quantities that are
/// translated to `exchange` elements in the EcoSpold 1 format. This class is a
/// wrapper to pull common information from flow quantities for the construction
/// of such exchange elements.
sealed interface FlowQuant {

	int number();

	@NonNull
	Flow flow();

	/// The raw amount of the quantity, without applying a possible conversion
	/// factor.
	double amount();

	@NonNull
	Unit unit();

	@Nullable
	Uncertainty uncertainty();

	/// Describes a possible target flow, if a flow mapping is applied for this
	/// flow.
	@Nullable
	FlowRef mapping();

	/// A possible unit conversion factor that needs to be applied on the flow
	/// amounts. This factor is only relevant if this quantity is related to a
	/// mapped flow and must be `1` otherwise.
	double factor();

	@Nullable
	static FlowQuant of(Exchange exchange, Map<String, FlowMapEntry> mappings) {
		if (exchange == null
			|| exchange.flow == null
			|| exchange.unit == null)
			return null;
		var mapping = mappingOf(exchange.flow, mappings);
		if (mapping == null)
			return new ExchangeQuant(exchange, null, 1.0);
		double f = factorOf(mapping, exchange.unit, exchange.flowPropertyFactor);
		return new ExchangeQuant(exchange, mapping.targetFlow(), f);
	}

	@Nullable
	static FlowQuant of(ImpactFactor factor, Map<String, FlowMapEntry> mappings) {
		if (factor == null
			|| factor.flow == null
			|| factor.unit == null)
			return null;
		var mapping = mappingOf(factor.flow, mappings);
		if (mapping == null)
			return new ImpactQuant(factor, null, 1.0);
		double f = factorOf(mapping, factor.unit, factor.flowPropertyFactor);
		// characterization factors are per unit of flow, thus, we also need to
		// invert a possible unit conversion factor
		if (f != 0 && f != 1) {
			f = 1 / f;
		}
		return new ImpactQuant(factor, mapping.targetFlow(), f);
	}

	@Nullable
	private static FlowMapEntry mappingOf(
		@NonNull Flow flow, Map<String, FlowMapEntry> mappings
	) {
		if (mappings == null)
			return null;
		var e = mappings.get(flow.refId);
		if (e == null
			|| e.targetFlow() == null
			|| e.targetFlow().flow == null)
			return null;
		return e;
	}

	private static double factorOf(
		@NonNull FlowMapEntry e, @NonNull Unit unit, FlowPropertyFactor fpf
	) {
		double f = e.factor();


		// TODO: we need to check and may convert units here!
		return f;
	}

	record ExchangeQuant(
		Exchange value,
		FlowRef mapping,
		double factor
	) implements FlowQuant {

		@Override
		public int number() {
			return (int) value.id;
		}

		@Override
		public Flow flow() {
			return value.flow;
		}

		@Override
		public double amount() {
			return value.amount;
		}

		@Override
		public Unit unit() {
			return value.unit;
		}

		@Override
		public @Nullable Uncertainty uncertainty() {
			return value.uncertainty;
		}
	}

	record ImpactQuant(
		ImpactFactor value,
		FlowRef mapping,
		double factor
	) implements FlowQuant {

		@Override
		public int number() {
			return (int) value.id;
		}

		@Override
		public Flow flow() {
			return value.flow;
		}

		@Override
		public double amount() {
			return value.value;
		}

		@Override
		public Unit unit() {
			return value.unit;
		}

		@Override
		public @Nullable Uncertainty uncertainty() {
			return value.uncertainty;
		}
	}
}
