package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The provider linker is used for collecting provider links during the import
 * and creating the links after the import is finished.
 */
public class ProviderLinker {

	/** Describes a provider link. */
	private class Link {

		/** The reference ID of the process. */
		final String process;

		/** The process internal ID of the exchange. */
		final int exchange;

		/** The reference ID of the exchange. */
		final String provider;

		Link(String process, int exchange, String provider) {
			this.process = process;
			this.exchange = exchange;
			this.provider = provider;
		}

		boolean valid() {
			return process != null
					&& exchange >= 0
					&& provider != null;
		}
	}

	private List<Link> links = new ArrayList<>();

	/**
	 * Adds a new link that should be created later (see
	 * {@link #createLinks(IDatabase)}).
	 * 
	 * @param processRefID
	 *            The reference ID of the process that contains the exchange.
	 * @param internalExchangeID
	 *            The process internal ID of the exchange that should be linked
	 *            to the provider.
	 * @param providerRefID
	 *            The reference ID of the provider process that should be linked
	 *            to the exchange.
	 */
	public void addLink(
			String processRefID,
			int internalExchangeID,
			String providerRefID) {
		Link link = new Link(processRefID, internalExchangeID, providerRefID);
		if (link.valid()) {
			links.add(link);
		}
	}

	public void createLinks(IDatabase db) {
		if (db == null || links.isEmpty())
			return;
		try {

			db.getEntityFactory().getCache().evictAll();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to create provider links", e);
		}
	}

}
