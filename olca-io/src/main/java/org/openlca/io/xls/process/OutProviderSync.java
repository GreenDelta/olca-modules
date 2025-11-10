package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;

class OutProviderSync implements OutEntitySync {

	private final OutConfig config;
	private final Set<Process> providers = new HashSet<>();

	OutProviderSync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Process p) {
			providers.add(p);
		}
	}

	@Override
	public void flush() {
		var cursor = config.createSheet(Tab.PROVIDERS)
			.withColumnWidths(4, 25)
			.header(
				Field.UUID,
				Field.NAME,
				Field.CATEGORY,
				Field.LOCATION);
		for (var p : Out.sort(providers)) {
			cursor.next(row ->
				row.next(p.refId)
					.next(p.name)
					.next(Out.pathOf(p))
					.next(p.location != null
						? p.location.name
						: null));
		}
	}
}
