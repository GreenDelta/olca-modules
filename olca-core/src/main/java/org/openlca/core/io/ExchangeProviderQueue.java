package org.openlca.core.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;

/**
 * A queue of exchanges that wait for a provider update.
 *
 * When importing a set of processes, these processes are often in arbitrary
 * order. This is a problem when exchanges have a link to a default provider but
 * that default provider was not imported yet. We then need to remember to
 * update this exchange after the import of the respective provider process
 * because we need the database internal ID of that process which is only
 * available after the import.
 */
public class ExchangeProviderQueue {

	private final IDatabase db;

	/**
	 * The IDs of the processes that are already saved in the database.
	 */
	private final Map<String, Long> processIds = new HashMap<>();

	/**
	 * Exchanges that wait for a provider: provider-id -> exchanges.
	 */
	private final HashMap<String, List<Exchange>> queue = new HashMap<>();

	private ExchangeProviderQueue(IDatabase db) {
		this.db = Objects.requireNonNull(db);
		db.getDescriptors(Process.class)
			.forEach(d -> processIds.put(d.refId, d.id));
	}

	public static ExchangeProviderQueue create(IDatabase db) {
		return new ExchangeProviderQueue(db);
	}

	/**
	 * Handle the given exchange. If a process with the given ID is already
	 * contained in the database, it will directly set the default provider ID of
	 * the exchange to the ID of that process. Otherwise, the exchange will be
	 * added to the queue until a process with the given ID is available.
	 *
	 * @param providerId the reference ID of the provider process
	 * @param exchange   the exchange that should be linked to the provider
	 */
	public void add(String providerId, Exchange exchange) {
		if (providerId == null || exchange == null)
			return;
		var processId = processIds.get(providerId);
		if (processId != null) {
			exchange.defaultProviderId = processId;
			return;
		}
		queue.computeIfAbsent(providerId, $ -> new ArrayList<>())
			.add(exchange);
	}

	/**
	 * Updates the exchanges that link to the given process and removes them from
	 * this queue. The given process needs to be saved in the database ({@code id
	 * != 0}).
	 *
	 * @param process a process that was saved in the database.
	 */
	public void pop(Process process) {
		if (process == null || process.refId == null || process.id == 0)
			return;
		processIds.put(process.refId, process.id);
		var exchanges = queue.remove(process.refId);
		if (exchanges == null || exchanges.isEmpty())
			return;
		db.transaction(em -> {
			for (var exchange : exchanges) {
				exchange.defaultProviderId = process.id;
				em.merge(exchange);
			}
		});
	}

}
