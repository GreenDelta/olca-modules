package org.openlca.io.openepd.io;

import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.ImpactCategory;
import org.openlca.util.Strings;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

final class ExportMapping {

	private ExportMapping() {
	}

	static MappingModel build(Epd epd) {
		if (epd == null)
			return MappingModel.empty();

		// init the mappings
		var mappings = new ArrayList<MethodMapping>();
		for (var mod : epd.modules) {
			var mapping = of(mod, mappings);
			if (mapping == null)
				continue;
			for (var impact : mod.result.impactResults) {
				var indicator = impact.indicator;
				if (indicator == null)
					continue;
				var row = rowOf(indicator, mapping);
				row.values().put(scopeOf(mod), mod.multiplier * impact.amount);
			}
		}

		// sort & map
		for (var model : mappings) {
			model.scopes().sort(Strings::compare);
			model.entries().sort((r1, r2) -> {
				var i1 = r1.indicator();
				var i2 = r2.indicator();
				return i1 != null && i2 != null
					? Strings.compare(i1.name, i2.name)
					: 0;
			});
			initMappings(model);
		}
		return new MappingModel(mappings);
	}

	private static MethodMapping of(EpdModule mod, List<MethodMapping> models) {
		if (mod == null
			|| Strings.nullOrEmpty(mod.name)
			|| mod.result == null
			|| mod.result.impactResults.isEmpty())
			return null;
		var method = mod.result.impactMethod;
		MethodMapping mapping = null;
		for (var existing : models) {
			if (Objects.equals(method, existing.method())) {
				mapping = existing;
				break;
			}
		}
		if (mapping == null) {
			mapping = new MethodMapping().method(method);
			models.add(mapping);
		}
		var scope = scopeOf(mod);
		if (!mapping.scopes().contains(scope)) {
			mapping.scopes().add(scope);
		}

		// find an EPD method
		if (method != null) {
			Vocab.Method candidate = null;
			double score = 0.0001;
			for (var m : Vocab.Method.values()) {
				var nextScore = m.matchScoreOf(method.name);
				if (nextScore > score) {
					candidate = m;
					score = nextScore;
				}
			}
			mapping.epdMethod(candidate);
		}

		return mapping;
	}

	private static String scopeOf(EpdModule mod) {
		if (mod == null || mod.name == null)
			return "?";
		var scope = Vocab.findScope(mod.name);
		return scope.isPresent()
			? scope.get().code()
			: mod.name;
	}

	private static IndicatorMapping rowOf(
		ImpactCategory indicator, MethodMapping mapping) {
		for (var row : mapping.entries()) {
			if (Objects.equals(indicator, row.indicator()))
				return row;
		}
		var row = new IndicatorMapping().indicator(indicator);
		mapping.entries().add(row);
		return row;
	}

	private static void initMappings(MethodMapping model) {

		var queue = EnumSet.allOf(Vocab.Indicator.class);
		Supplier<Vocab.Indicator> next = () -> {
			var i = queue.iterator().next();
			queue.remove(i);
			return i;
		};

		while (!queue.isEmpty()) {
			var epdInd = next.get();
			var match = Match.empty();
			for (var row : model.entries()) {
				var nextMatch = Match.of(epdInd, row);
				if (match.isBetterThan(nextMatch))
					continue;
				var prevMatch = Match.of(row);
				if (prevMatch.isBetterThan(nextMatch))
					continue;

				match.release();
				if (prevMatch.isBound()) {
					if (prevMatch.indicator() != epdInd) {
						queue.add(prevMatch.indicator());
					}
					prevMatch.release();
				}
				nextMatch.bind();
				match = nextMatch;
			}
		}
	}

	private record Match(
		Vocab.Indicator indicator,
		IndicatorMapping row,
		Vocab.UnitMatch unit,
		double score) {

		private static final Match _empty = new Match(null, null, null, 0);

		static Match empty() {
			return _empty;
		}

		static Match of(IndicatorMapping row) {
			return of(row.epdIndicator(), row);
		}

		static Match of(Vocab.Indicator indicator, IndicatorMapping row) {
			if (indicator == null || row == null || row.indicator() == null)
				return empty();
			var refUnit = row.indicator().referenceUnit;
			var unitMatch = indicator.unitMatchOf(refUnit)
				.orElse(null);
			if (unitMatch == null)
				return empty();
			var score = Vocab.codesEqual(indicator.code(), row.indicator().code)
				? 1.0
				: indicator.matchScoreOf(row.indicator().name);
			if (score < 1e-4)
				return empty();
			return new Match(indicator, row, unitMatch, score);
		}

		boolean isEmpty() {
			return indicator == null
				|| row == null
				|| unit == null;
		}

		boolean isBetterThan(Match other) {
			if (isEmpty())
				return false;
			return other.isEmpty() || score > other.score;
		}

		void bind() {
			if (isEmpty())
				return;
			row.epdIndicator(indicator)
				.unit(unit)
				.factor(unit.factor());
		}

		boolean isBound() {
			return !isEmpty() && row.epdIndicator() == indicator;
		}

		void release() {
			if (!isBound())
				return;
			row.epdIndicator(null)
				.unit(null)
				.factor(1);
		}

	}
}
