package org.openlca.io.hestia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Source;
import org.openlca.util.KeyGen;
import org.openlca.util.Res;

class SourceFetch {

	private final ImportLog log;
	private final HestiaClient client;
	private final IDatabase db;

	private SourceFetch(ImportLog log, HestiaClient client, IDatabase db) {
		this.log = log;
		this.client = client;
		this.db = db;
	}

	static SourceFetch of(ImportLog log, HestiaClient client, IDatabase db) {
		return new SourceFetch(log, client, db);
	}

	List<Source> get(Cycle cycle) {
		var refs = sourceRefsOf(cycle);
		if (refs.isEmpty())
			return List.of();

		var sources = new ArrayList<Source>(refs.size());
		var threads = Math.min(10, refs.size());
		try (var pool = Executors.newFixedThreadPool(threads)) {
			var futures = refs.stream()
					.map(ref -> pool.submit(() -> fetchAndMap(ref)))
					.toList();
			for (var f : futures) {
				var res = f.get();
				if (res.hasError()) {
					log.error("failed to fetch source");
					continue;
				}
				sources.add(res.value());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception ignored) {
		}
		return sources;
	}

	private List<HestiaRef> sourceRefsOf(Cycle cycle) {
		if (cycle == null)
			return List.of();
		var refs = new ArrayList<HestiaRef>();
		var ds = cycle.defaultSource();
		if (ds != null) {
			refs.add(ds);
		}
		refs.addAll(cycle.aggregatedSources());
		return refs;
	}

	private Res<Source> fetchAndMap(HestiaRef ref) {
		var res = client.getSource(ref.id());
		if (res.hasError())
			return res.castError();
		var s = res.value();
		var refId = KeyGen.get("hestia-source", s.id());
		var source = db.get(Source.class, refId);
		if (source != null)
			return Res.of(source);

		source = Source.of(s.name());
		source.refId = refId;
		var bib = s.bibliography();
		if (bib != null) {
			source.textReference = bib.title();
			source.url = bib.articlePdf();
			var year = bib.year();
			if (year != null) {
				source.year = year.shortValue();
			}
			source.description = bib.outlet();
		}
		db.insert(source);
		return Res.of(source);
	}
}
