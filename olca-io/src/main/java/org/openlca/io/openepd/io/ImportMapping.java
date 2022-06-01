package org.openlca.io.openepd.io;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.io.openepd.EpdDoc;
import org.openlca.io.openepd.EpdIndicatorResult;
import org.openlca.io.openepd.io.Vocab.Indicator;
import org.openlca.io.openepd.io.Vocab.Method;
import org.openlca.io.openepd.io.Vocab.UnitMatch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Stream;

final class ImportMapping {

	private ImportMapping() {
	}

	static MappingModel build(EpdDoc doc, IDatabase db) {
		if (doc == null)
			return MappingModel.empty();

		var mappings = new ArrayList<MethodMapping>();
		var methods = db.getAll(ImpactMethod.class);
		for (var r : doc.impactResults) {
			var epdMethod = Method.of(r.method()).orElse(null);
			if (epdMethod == null)
				continue;
			var mapping = new MethodMapping()
				.epdMethod(epdMethod)
				.method(find(methods, epdMethod));
			mappings.add(mapping);
			bind(mapping, Stream.of(
				r.results(), doc.outputFlows, doc.resourceUses));
		}
		return new MappingModel(mappings);
	}

	private static void bind(
		MethodMapping mapping, Stream<List<EpdIndicatorResult>> stream) {

		var map = new EnumMap<Indicator, EpdIndicatorResult>(Indicator.class);
		stream.flatMap(Collection::stream)
			.forEach(r -> Indicator.of(r.indicator())
				.ifPresent(i -> map.put(i, r)));

		// init mapping entries and fill scopes
		var scopes = new TreeSet<String>();
		for (var e : map.entrySet()) {
			var epdIndicator = e.getKey();
			var entry = new IndicatorMapping()
				.epdIndicator(epdIndicator);
			mapping.entries().add(entry);
			for (var v : e.getValue().values()) {
				if (v.scope() != null
					&& v.value() != null
					&& v.value().mean() != null) {
					scopes.add(v.scope());
					entry.values().put(v.scope(), v.value().mean());
				}
			}
		}
		mapping.scopes().addAll(scopes);
		initMappings(mapping);
	}

	static void initMappings(MethodMapping mapping) {
		var method = mapping.method();
		if (method == null)
			return;
		var queue = new ArrayDeque<>(method.impactCategories);
		while (!queue.isEmpty()) {
			var indicator = queue.poll();
			var match = Match.empty();
			for (var row : mapping.entries()) {
				var nextMatch = Match.of(indicator, row);
				if (match.isBetterThan(nextMatch))
					continue;
				var prevMatch = Match.of(row);
				if (prevMatch.isBetterThan(nextMatch))
					continue;

				match.release();
				if (prevMatch.isBound()) {
					if (!Objects.equals(prevMatch.indicator, indicator)) {
						queue.add(prevMatch.indicator);
					}
					prevMatch.release();
				}
				nextMatch.bind();
				match = nextMatch;
			}
		}
	}

	private static ImpactMethod find(
		List<ImpactMethod> methods, Method epdMethod) {
		ImpactMethod candidate = null;
		double score = 0.0001;
		for (var method : methods) {
			if (Vocab.codesEqual(epdMethod.code(), method.code))
				return method;
			var nextScore = epdMethod.matchScoreOf(method.name);
			if (nextScore > score) {
				candidate = method;
			}
		}
		return candidate;
	}

	private record Match(
		ImpactCategory indicator,
		IndicatorMapping row,
		UnitMatch unit,
		double score) {

		private static final Match _empty = new Match(null, null, null, 0);

		static Match empty() {
			return _empty;
		}

		static Match of(IndicatorMapping row) {
			return of(row.indicator(), row);
		}

		static Match of(ImpactCategory indicator, IndicatorMapping row) {
			if (indicator == null || row == null || row.epdIndicator() == null)
				return empty();
			var epdInd = row.epdIndicator();
			var unitMatch = epdInd.unitMatchOf(indicator.referenceUnit)
				.orElse(null);
			if (unitMatch == null)
				return empty();
			var score = Vocab.codesEqual(indicator.code, epdInd.code())
				? 1.0
				: epdInd.matchScoreOf(indicator.name);
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
			row.indicator(indicator)
				.unit(unit)
				.factor(1 / unit.factor());
		}

		boolean isBound() {
			return !isEmpty()
				&& Objects.equals(indicator, row.indicator());
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
