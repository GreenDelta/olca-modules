package org.openlca.io.openepd.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;

public record MethodMapping(
	String code,
	ImpactMethod method,
	List<IndicatorMapping> indicatorMappings) {

	public static MethodMapping emptyOf(String code, Collection<IndicatorKey> keys) {
		var indicatorMappings = keys.stream()
			.map(IndicatorMapping::emptyOf)
			.toList();
		return new MethodMapping(code, null, indicatorMappings);
	}

	public static MethodMapping init(
		String code, ImpactMethod method, Collection<IndicatorKey> keys) {
		var mappings = new ArrayList<IndicatorMapping>();
		for (var key : keys) {
			ImpactCategory impact = null;
			for (var i : method.impactCategories) {
				if (ImpactMapping.sameCode(key.code(), i.code)) {
					impact = i;
					break;
				}
				if (ImpactMapping.sameCode(key.code(), i.name)) {
					// map by name alternatively but prefer codes
					impact = i;
				}
			}
			mappings.add(new IndicatorMapping(key, impact));
		}
		return new MethodMapping(code, method, mappings);
	}

	public boolean isEmpty() {
		return method == null;
	}

	public List<IndicatorKey> keys() {
		return indicatorMappings.stream()
			.map(IndicatorMapping::key)
			.sorted()
			.toList();
	}

	public IndicatorMapping getIndicatorMapping(IndicatorKey key) {
		for (var i : indicatorMappings) {
			if (Objects.equals(key, i.key()))
				return i;
		}
		return IndicatorMapping.emptyOf(key);
	}
}
