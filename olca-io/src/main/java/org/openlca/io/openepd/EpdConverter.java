package org.openlca.io.openepd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.util.Categories;
import org.openlca.util.Pair;
import org.openlca.util.Strings;

/**
 * A utility class for converting openLCA EPDs into the openEPD format.
 */
public final class EpdConverter {

	private EpdConverter() {
	}

	/**
	 * Checks if the given EPD model can be converted into on openEPD document.
	 */
	public static Validation validate(Epd epd) {
		if (epd == null)
			return Validation.error("The EPD is empty.");
		if (epd.product == null || epd.product.flow == null)
			return Validation.error("The EPD has no product.");
		if (epd.product.unit == null)
			return Validation.error("The EPD has no declared unit");
		if (epd.product.amount == 0)
			return Validation.error("The product amount is 0.");
		return Validation.ok();
	}

	public static EpdDoc toEpdDoc(Epd epd) {
		var doc = new EpdDoc();
		doc.isPrivate = true;
		doc.version = 1;
		doc.productName = epd.name;
		if (epd.product != null && epd.product.flow != null) {
			doc.productDescription = epd.product.flow.description;
		}
		doc.lcaDiscussion = epd.description;

		// declared unit
		if (epd.product != null && epd.product.unit != null) {
			doc.declaredUnit = new EpdQuantity(
				epd.product.amount, epd.product.unit.name);
		}
		var mass = massInKgOf(epd.product);
		if (mass.isPresent()) {
			doc.kgPerDeclaredUnit = new EpdQuantity(
				mass.getAsDouble(), "kg");
		}

		// category
		if (epd.category != null) {
			var path = Categories.path(epd.category);
			if (!path.isEmpty()) {
				doc.productClasses.add(
					Pair.of("io.cqd.ec3", String.join(" >> ", path)));
			}
		}

		doc.manufacturer = toOrg(epd.manufacturer);
		doc.verifier = toOrg(epd.verifier);
		doc.programOperator = toOrg(epd.programOperator);
		doc.pcr = toPcr(epd.pcr);

		doc.impactResults.addAll(resultsOf(epd));
		return doc;
	}

	public static EpdOrg toOrg(Actor actor) {
		if (actor == null)
			return null;
		var org = new EpdOrg();
		org.name = actor.name;
		org.webDomain = actor.website;
		return org;
	}

	public static EpdPcr toPcr(Source source) {
		if (source == null)
			return null;
		var pcr = new EpdPcr();
		pcr.id = source.refId;
		pcr.name = source.name;
		return pcr;
	}

	public static Collection<EpdImpactResult> resultsOf(Epd epd) {
		var map = new HashMap<String, EpdImpactResult>();
		for (var mod : epd.modules) {
			var result = mod.result;
			if (result == null
				|| result.impactMethod == null
				|| Strings.nullOrEmpty(result.impactMethod.code))
				continue;
			var docResult = map.computeIfAbsent(
				result.impactMethod.code,
				code -> new EpdImpactResult(code, new ArrayList<>()));

			for (var impact : result.impactResults) {
				if (impact.indicator == null
					|| Strings.nullOrEmpty(impact.indicator.code))
					continue;
				var code = impact.indicator.code;
				EpdIndicatorResult docImpact = null;
				for (var i : docResult.results()) {
					if (Objects.equals(code, i.indicator())) {
						docImpact = i;
						break;
					}
				}
				if (docImpact == null) {
					docImpact = new EpdIndicatorResult(code, new ArrayList<>());
					docResult.results().add(docImpact);
				}
				var value = EpdMeasurement.of(
					mod.multiplier * impact.amount,
					impact.indicator.referenceUnit);
				docImpact.values().add(new EpdScopeValue(mod.name, value));
			}
		}
		return map.values();
	}

	/**
	 * Tries to infer the mass in kilograms per declared unit.
	 */
	public static OptionalDouble massInKgOf(EpdProduct product) {
		if (product == null
			|| product.flow == null
			|| product.property == null
			|| product.unit == null)
			return OptionalDouble.empty();

		BiPredicate<String, RefEntity> matches = (name, entity) -> {
			if (entity == null || entity.name == null)
				return false;
			var eName = entity.name.trim();
			return eName.equalsIgnoreCase(name);
		};

		Function<FlowProperty, Unit> getKg = prop -> {
			if (prop.unitGroup == null)
				return null;
			for (var unit : prop.unitGroup.units) {
				if (matches.test("kg", unit))
					return unit;
			}
			return null;
		};

		// the property of the product is mass
		if (matches.test("Mass", product.property)) {
			if (matches.test("kg", product.unit))
				return OptionalDouble.of(product.amount);

			var kg = getKg.apply(product.property);
			if (kg == null)
				return OptionalDouble.empty();

			var amount = product.amount * product.unit.conversionFactor
				/ kg.conversionFactor;
			return OptionalDouble.of(amount);
		}

		// search for Mass and kg
		var massFac = product.flow.flowPropertyFactors.stream()
			.filter(f -> matches.test("Mass", f.flowProperty))
			.findAny()
			.orElse(null);
		if (massFac == null || massFac.flowProperty == null)
			return OptionalDouble.empty();
		var kg = getKg.apply(massFac.flowProperty);
		var propFac = product.flow.getFactor(product.property);
		if (kg == null || propFac == null)
			return OptionalDouble.empty();

		var amount = product.amount * product.unit.conversionFactor
			* massFac.conversionFactor
			/ (kg.conversionFactor * propFac.conversionFactor);
		return OptionalDouble.of(amount);
	}

	public record Validation(String error) {

		static Validation ok() {
			return new Validation(null);
		}

		static Validation error(String message) {
			return new Validation(message);
		}

		public boolean hasError() {
			return error != null;
		}

		public boolean isOk() {
			return error == null;
		}

	}
}
