package org.openlca.io.hestia;

import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ProviderMap;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProviderType;
import org.openlca.util.Exchanges;

class ProviderResolver {

	private final IDatabase db;
	private ProviderMap providers;

	private ProviderResolver (IDatabase db) {
		this.db = db;
	}

	static ProviderResolver of(IDatabase db) {
		return new ProviderResolver(db);
	}

	void resolve(Process process, Exchange exchange) {
		if (!Exchanges.isLinkable(exchange))
			return;
		if (providers == null) {
			providers = ProviderMap.create(db);
		}
		Process provider = null;
		int score = -1;
		for (var techFlow : providers.getProvidersOf(exchange.flow.id)) {
			if (!techFlow.isProcess())
				continue;
			var next = db.get(Process.class, techFlow.providerId());
			int nextScore = score(process, next);
			if (nextScore > score) {
				score = nextScore;
				provider = next;
			}
		}

		if (provider != null) {
			exchange.defaultProviderId = provider.id;
			exchange.defaultProviderType = ProviderType.PROCESS;
		}
	}

	private int score(Process process, Process provider) {
		int score = 0;

		if (provider.location != null && Objects.equals(
				process.location, provider.location)) {
			score += 4;
		} else if (isGlobal(provider.location)) {
			score += 2;
		}

		if (Strings.isNotBlank(provider.name)) {
			var name = provider.name.trim().toLowerCase();
			if (name.startsWith("market ")) {
				score += 3;
			}
		}
		return score;
	}

	private boolean isGlobal(Location location) {
		if (location == null || Strings.isBlank(location.code))
			return false;
		var code = location.code.trim().toUpperCase();
		return "GLO".equals(code) || "ROW".equals(code);
	}
}
