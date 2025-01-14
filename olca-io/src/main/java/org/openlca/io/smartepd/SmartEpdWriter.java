package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Result;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartEpdWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Epd epd;

	private SmartEpdWriter(Epd epd) {
		this.epd = Objects.requireNonNull(epd);
	}

	public static SmartEpdWriter of(Epd epd) {
		return new SmartEpdWriter(epd);
	}

	public SmartEpd write() {
		var smartEpd = new SmartEpd();
		smartEpd.productName(epd.name)
			.productDescription(epd.description);
		update(smartEpd);
		return smartEpd;
	}

	public void update(SmartEpd smartEpd) {
		if (smartEpd == null)
			return;

		var mappings = MethodMapping.getAll();
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
			var mapping = findMapping(mod.result, mappings);
			if (mapping == null) {
				log.warn("no mapping found for module: {}", mod.name);
				continue;
			}

			// collect the results
			for (var r : mod.result.impactResults) {
				var indicator = findIndicator(r, mapping);
				if (indicator == null) {
					log.warn("no mapping found for indicator: {}", r.indicator);
					continue;
				}
				results.put(
					mapping.smartEpd(),
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
			var result = results.resultOf(type);
			if (result != null && !result.results().isEmpty()) {
				smartEpd.put(type, result);
			}
		}
	}

	private SmartIndicator findIndicator(ImpactResult r, MethodMapping mapping) {
		if (r == null || r.indicator == null || r.indicator.refId == null)
			return null;
		for (var i : mapping.indicators()) {
			if (i.ref() == null)
				continue;
			if (Objects.equals(i.ref().id(), r.indicator.refId)) {
				var indicator = SmartIndicator.of(i.smartEpd()).orElse(null);
				if (indicator != null)
					return indicator;
			}
		}
		return null;
	}

	private MethodMapping findMapping(
		Result result, List<MethodMapping> mappings
	) {
		if (result == null || mappings == null)
			return null;

		var indicatorIds = new HashSet<String>();
		for (var i : result.impactResults) {
			if (i.indicator == null || i.indicator.refId == null)
				continue;
			indicatorIds.add(i.indicator.refId);
		}

		MethodMapping mapping = null;
		int matchCount = 0;
		for (var m : mappings) {
			int count = 0;
			for (var i : m.indicators()) {
				if (i.ref() == null)
					continue;
				if (indicatorIds.contains(i.ref().id())
					&& Strings.notEmpty(i.smartEpd())) {
					count++;
				}
			}
			if (count > matchCount) {
				matchCount = count;
				mapping = m;
			}
		}
		return mapping;
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

		void put(String method, SmartIndicator indicator, SmartModuleValue value) {
			if (method == null || indicator == null || value == null)
				return;
			var list = results.computeIfAbsent(indicator, $ -> new ArrayList<>());
			SmartResult result;
			if (indicator.isImpact()) {
				result = list.stream()
					.filter(r -> r.method().equals(method))
					.findFirst()
					.orElseGet(() -> {
						var r = new SmartResult()
							.method(method)
							.impact(indicator.id())
							.unit(indicator.unit());
						list.add(r);
						return r;
					});
			} else {
				if (!list.isEmpty()) {
					result = list.getFirst();
				} else {
					result = new SmartResult()
						.indicator(indicator.id())
						.unit(indicator.unit());
					list.add(result);
				}
			}
			value.addTo(result.json());
		}

		SmartResultList resultOf(SmartIndicatorType type) {
			var rs = new ArrayList<SmartResult>();
			for (var e : results.entrySet()) {
				var indicator = e.getKey();
				if (e.getValue() == null
					|| indicator == null
					|| indicator.type() != type)
					continue;
				rs.addAll(e.getValue());
			}
			return new SmartResultList(type).results(rs);
		}
	}
}
