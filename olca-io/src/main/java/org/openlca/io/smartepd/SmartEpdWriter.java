package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Result;
import org.openlca.io.openepd.EpdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartEpdWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Epd epd;
	private final List<SmartMethodMapping> methods;
	private final List<SmartIndicatorMapping> indicators;

	private SmartEpdWriter(Epd epd) {
		this.epd = Objects.requireNonNull(epd);
		this.methods = SmartMethodMapping.getDefault();
		this.indicators = SmartIndicatorMapping.getDefault();
	}

	public static SmartEpdWriter of(Epd epd) {
		return new SmartEpdWriter(epd);
	}

	public SmartEpd write() {
		var smartEpd = new SmartEpd();
		smartEpd.productName(epd.name)
				.productDescription(Strings.cutEnd(epd.description, 2000));

		// declared unit
		var unit = getDeclaredUnit();
		if (unit != null) {
			smartEpd.declaredUnit(unit);
			EpdConverter.massInKgOf(epd.product)
					.ifPresent(smartEpd::massPerUnit);
		}

		// write results
		update(smartEpd);

		return smartEpd;
	}

	public void update(SmartEpd smartEpd) {
		if (smartEpd == null)
			return;

		var results = new ResultMap();
		var mods = EnumSet.noneOf(SmartModule.class);

		for (var mod : epd.modules) {
			if (mod.result == null || mod.result.impactResults.isEmpty())
				continue;

			// find the corresponding SmartEPD module
			var smartMod = SmartModule.of(mod.name).orElse(null);
			if (smartMod == null) {
				log.warn("module {} cannot mapped to a SmartEPD module", mod.name);
				continue;
			}
			mods.add(smartMod);

			// find the method mapping
			var method = findMethod(mod.result);
			if (method == null) {
				log.warn("no method mapping found for module: {}", mod.name);
				method = SmartMethod.UNKNOWN;
			}

			// collect the results
			for (var r : mod.result.impactResults) {
				var indicator = findIndicator(r);
				if (indicator == null) {
					log.warn("no mapping found for indicator: {}", r.indicator);
					continue;
				}
				results.put(
						method,
						indicator,
						new SmartModuleValue(smartMod, r.amount));
			}
		}

		if (results.isEmpty()) {
			log.warn("no indicator results could be mapped");
			return;
		}

		SmartModuleInfo.write(smartEpd, mods);
		for (var type : SmartIndicatorType.values()) {
			var list = results.resultOf(type);
			if (!list.isEmpty()) {
				smartEpd.putResultsAndLists(type, list);
			}
		}
	}

	private SmartIndicator findIndicator(ImpactResult r) {
		if (r == null || r.indicator == null)
			return null;

		// find by code
		var code = r.indicator.code;
		if (Strings.isNotBlank(code)) {
			var smartIndicator = SmartIndicator.of(code).orElse(null);
			if (smartIndicator != null)
				return smartIndicator;
		}

		// find by name
		var name = r.indicator.name;
		if (Strings.isNotBlank(name)) {
			var smartIndicator = SmartIndicator.of(name).orElse(null);
			if (smartIndicator != null)
				return smartIndicator;
		}

		// find by mapping ID
		var refId = r.indicator.refId;
		if (Strings.isBlank(refId))
			return null;
		for (var i : indicators) {
			for (var ref : i.refs()) {
				if (Objects.equals(ref.id(), refId)) {
					return i.indicator();
				}
			}
		}
		return null;
	}

	private SmartMethod findMethod(Result result) {
		if (result == null)
			return null;

		// if there is a method defined in the result, first try to find a
		// mapping for this method
		if (result.impactMethod != null) {
			var method = result.impactMethod;

			// check the code
			if (Strings.isNotBlank(method.code)) {
				var smartMethod = SmartMethod.of(method.code).orElse(null);
				if (smartMethod != null)
					return smartMethod;
			}

			// check the name
			var smartMethod = SmartMethod.of(method.name).orElse(null);
			if (smartMethod != null)
				return smartMethod;

			// search mappings for the method ID
			for (var m : methods) {
				if (SmartRef.matches(m.ref(), result.impactMethod))
					return m.method();
			}
		}

		// if no method is defined in the result, try to find the best mapping for
		// the used indicators in the result
		var indicatorIds = new HashSet<String>();
		for (var i : result.impactResults) {
			if (i.indicator == null || i.indicator.refId == null)
				continue;
			indicatorIds.add(i.indicator.refId);
		}

		SmartMethodMapping mapping = null;
		int matchCount = 0;
		for (var m : methods) {
			int count = 0;
			for (var i : m.indicators()) {
				if (indicatorIds.contains(i)) {
					count++;
				}
			}
			if (count > matchCount) {
				matchCount = count;
				mapping = m;
			}
		}
		return mapping != null ? mapping.method() : null;
	}

	private SmartRefUnit getDeclaredUnit() {
		if (epd.product == null || epd.product.unit == null)
			return null;
		return new SmartRefUnit()
				.unit(epd.product.unit.name)
				.qty(epd.product.amount);
	}

	private record ResultMap(
			Map<SmartIndicator, List<SmartResult>> results
	) {

		ResultMap() {
			this(new EnumMap<>(SmartIndicator.class));
		}

		boolean isEmpty() {
			return results.isEmpty();
		}

		void put(
				SmartMethod method, SmartIndicator indicator, SmartModuleValue value
		) {
			if (method == null || indicator == null || value == null)
				return;
			var list = results.computeIfAbsent(indicator, $ -> new ArrayList<>());
			SmartResult result;
			if (indicator.isImpact()) {
				result = list.stream()
						.filter(r -> r.method().equals(method.id()))
						.findFirst()
						.orElseGet(() -> {
							var r = new SmartResult()
									.method(method.id())
									.impact(indicator.id())
									.unit(indicator.unitFor(method));
							list.add(r);
							return r;
						});
			} else {
				if (!list.isEmpty()) {
					result = list.getFirst();
				} else {
					result = new SmartResult()
							.indicator(indicator.id())
							.unit(indicator.defaultUnit());
					list.add(result);
				}
			}
			value.addTo(result.json());
		}

		List<SmartResult> resultOf(SmartIndicatorType type) {
			var list = new ArrayList<SmartResult>();
			for (var e : results.entrySet()) {
				var indicator = e.getKey();
				if (e.getValue() == null
						|| indicator == null
						|| indicator.type() != type)
					continue;
				list.addAll(e.getValue());
			}
			return list;
		}
	}
}
