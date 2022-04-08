package org.openlca.io.openepd.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.io.openepd.EpdDoc;
import org.openlca.util.Strings;

import gnu.trove.map.hash.TLongObjectHashMap;

public record ImpactMapping(Map<String, MethodMapping> map) {

	public static ImpactMapping init(EpdDoc epd, IDatabase db) {

		// collect the method codes and related indicator keys
		var codes = new HashMap<String, Set<IndicatorKey>>();
		for (var result : epd.impactResults) {
			var code = result.method();
			var keys = codes.computeIfAbsent(code, _code -> new HashSet<>());
			for (var i : result.indicatorResults()) {
				var unit = i.values().stream()
					.filter(v -> v.value() != null)
					.map(v -> v.value().unit())
					.filter(Objects::nonNull)
					.findAny()
					.orElse("");
				keys.add(new IndicatorKey(i.indicator(), unit));
			}
		}

		// initialize the method mappings
		var methods = db.getAll(ImpactMethod.class);
		var map = new HashMap<String, MethodMapping>();
		for (var e : codes.entrySet()) {
			var code = e.getKey();
			var keys = e.getValue();
			ImpactMethod method = null;
			for (var m : methods) {
				if (sameCode(code, m.code)) {
					method = m;
					break;
				}
			}
			var mapping = method != null
				? MethodMapping.init(code, method, keys)
				: MethodMapping.emptyOf(code, keys);
			map.put(code, mapping);
		}

		return new ImpactMapping(map);
	}

	public static boolean sameCode(String code1, String code2) {
		if (Strings.nullOrEmpty(code1) || Strings.nullOrEmpty(code2))
			return false;
		return code1.trim().equalsIgnoreCase(code2.trim());
	}

	public MethodMapping getMethodMapping(String code) {
		var mapping = map.get(code);
		if (mapping != null)
			return mapping;
		var empty = MethodMapping.emptyOf(code, Collections.emptySet());
		map.put(code, empty);
		return empty;
	}

	public IndicatorMapping getIndicatorMapping(String methodCode, IndicatorKey key) {
		var m = getMethodMapping(methodCode);
		return m.getIndicatorMapping(key);
	}

	public List<String> methodCodes() {
		return map.keySet()
			.stream()
			.sorted()
			.toList();
	}

	public MethodMapping swapMethod(String code, ImpactMethod method) {
		var current = map.get(code);
		List<IndicatorKey> keys = current != null
			? current.keys()
			: Collections.emptyList();
		var next = method == null
			? MethodMapping.emptyOf(code, keys)
			: MethodMapping.init(code, method, keys);
		map.put(code, next);
		return next;
	}

	public void swapIndicator(
		String methodCode, IndicatorKey key, ImpactCategory impact) {
		var methodMapping = getMethodMapping(methodCode);
		if (methodMapping.isEmpty())
			return;
		var mappings = methodMapping.indicatorMappings();
		mappings.stream()
			.filter(m -> Objects.equals(m.key(), key))
			.findAny()
			.ifPresent(mappings::remove);
		mappings.add(new IndicatorMapping(key, impact));
	}

	/**
	 * Returns true if this mapping contains empty method or indicator mappings.
	 */
	public boolean hasEmptyMappings() {
		for (var m : map.values()) {
			if (m.isEmpty())
				return true;
			for (var i : m.indicatorMappings()) {
				if (i.isEmpty())
					return true;
			}
		}
		return false;
	}

	public void persistIn(IDatabase db) {
		var persisted = new HashMap<String, MethodMapping>();
		var updatedMethods = new TLongObjectHashMap<ImpactMethod>();
		var updatedIndicators = new TLongObjectHashMap<ImpactCategory>();

		for (var e : map.entrySet()) {
			var code = e.getKey();
			var mapping = e.getValue();
			if (mapping.isEmpty()) {
				persisted.put(code, mapping);
				continue;
			}

			// update indicator codes
			var indicatorMappings = new ArrayList<IndicatorMapping>();
			for (var i : mapping.indicatorMappings()) {
				if (i.isEmpty()) {
					indicatorMappings.add(i);
					continue;
				}
				var indicator = i.indicator();

				// if it already was updated, then do it just once and
				// take the updated version
				var updated = updatedIndicators.get(indicator.id);
				if (updated != null) {
					indicatorMappings.add(new IndicatorMapping(i.key(), updated));
					continue;
				}

				indicator.code = i.code();
				indicator = db.update(indicator);
				updatedIndicators.put(indicator.id, indicator);
				indicatorMappings.add(new IndicatorMapping(i.key(), indicator));
			}

			// update method code
			var method = mapping.method();
			var updated = updatedMethods.get(method.id);
			if (updated != null) {
				persisted.put(code, new MethodMapping(code, updated, indicatorMappings));
				continue;
			}
			if (sameCode(code, method.code)) {
				persisted.put(code, new MethodMapping(code, method, indicatorMappings));
				continue;
			}

			method.code = code;
			method = db.update(method);
			updatedMethods.put(method.id, method);
			persisted.put(code, new MethodMapping(code, method, indicatorMappings));
		}

		map.clear();
		map.putAll(persisted);
	}

}

