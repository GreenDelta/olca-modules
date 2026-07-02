package org.openlca.io.ecospold1.output;

import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Strings;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.io.maps.FlowRef;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.Descriptor;

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
		double f = factorOf(
			mapping, exchange.flow, exchange.unit, exchange.flowPropertyFactor);
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
		double f = factorOf(
			mapping, factor.flow, factor.unit, factor.flowPropertyFactor);
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
		@NonNull FlowMapEntry e,
		@NonNull Flow flow,
		@NonNull Unit unit,
		FlowPropertyFactor fpf
	) {
		var def = DefUnit.of(e, flow);
		return def != null
			? e.factor() * def.factor(unit, fpf)
			: e.factor();
	}

	/// The unit of the source flow for which the conversion factor of a flow
	/// mapping was defined.
	@NullMarked
	record DefUnit(Unit unit, FlowPropertyFactor property) {

		@Nullable
		static DefUnit of(FlowMapEntry e, Flow flow) {
			var def = e.sourceFlow();
			if (def == null || def.unit == null)
				return null;
			var prop = defPropertyOf(def, flow);
			if (prop == null || prop.flowProperty.unitGroup == null)
				return null;

			for (var unit : prop.flowProperty.unitGroup.units) {
				if (eq(def.unit, unit))
					return new DefUnit(unit, prop);
			}
			return null;
		}

		@Nullable
		private static FlowPropertyFactor defPropertyOf(FlowRef ref, Flow flow) {
			if (ref.property == null)
				return flow.getReferenceFactor();
			for (var f : flow.flowPropertyFactors) {
				if (eq(ref.property, f.flowProperty))
					return f;
			}
			return flow.getReferenceFactor();
		}

		private static boolean eq(Descriptor d, @Nullable RefEntity e) {
			if (e == null)
				return false;
			// identifying things by name is fine for flow properties & units
			return Strings.equalsIgnoreCase(d.refId, e.refId)
				|| Strings.equalsIgnoreCase(d.name, e.name);
		}

		double factor(Unit otherUnit, @Nullable FlowPropertyFactor otherProperty) {
			double uf = nullEq(this.unit, otherUnit) || this.unit.conversionFactor == 0
				? 1
				: otherUnit.conversionFactor / this.unit.conversionFactor;
			if (otherProperty == null
				|| otherProperty.conversionFactor == 0
				|| nullEq(this.property.flowProperty, otherProperty.flowProperty)) {
				return uf;
			}
			return uf * this.property.conversionFactor
				/ otherProperty.conversionFactor;
		}


		private static boolean nullEq(
			RefEntity thisEntity, @Nullable RefEntity otherEntity
		) {
			if (otherEntity == null)
				return true;
			return Strings.equalsIgnoreCase(thisEntity.refId, otherEntity.refId)
				|| Strings.equalsIgnoreCase(thisEntity.name, otherEntity.name);
		}

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
