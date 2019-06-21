package org.openlca.io.ilcd.input;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The provider linker is used for collecting provider links during the import
 * and creating the links after the import is finished. TODO: maybe we should
 * move this later to the core package and re-use this also in the JSON-LD
 * export... but this currently only works with Derby. Also note that this
 * currently does not check and assure that the given provider indeed provides
 * the flow of the respective exchange (however, wrong providers should not
 * taken into account in the openLCA linking algorithms).
 */
public class ProviderLinker {

	/** Describes a provider link. */
	private class Link {

		/** The reference ID of the process. */
		final String process;

		/** The process internal ID of the exchange. */
		final int exchange;

		/** The reference ID of the provider process. */
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

		@Override
		public String toString() {
			return "ProviderLink [ process=" + process
					+ " exchange=" + exchange
					+ " provider=" + provider + "]";
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
			Map<Long, Map<Integer, Long>> map = buildLinkMap(db);
			Connection con = db.createConnection();
			con.setAutoCommit(false);

			// prepare queries
			String querySql = "SELECT f_owner, internal_id FROM"
					+ " tbl_exchanges FOR UPDATE OF f_default_provider";
			Statement query = con.createStatement();
			query.setCursorName("UPDATE_CURSOR");
			ResultSet cursor = query.executeQuery(querySql);
			String updateSql = "UPDATE tbl_exchanges SET f_default_provider = ?"
					+ " WHERE CURRENT OF UPDATE_CURSOR";
			PreparedStatement update = con.prepareStatement(updateSql);

			// do updates
			while (cursor.next()) {
				Map<Integer, Long> imap = map.get(cursor.getLong(1));
				if (imap == null)
					continue;
				Long provider = imap.get(cursor.getInt(2));
				if (provider == null)
					continue;
				update.setLong(1, provider);
				update.executeUpdate();
			}

			// commit and clean up
			cursor.close();
			query.close();
			update.close();
			con.commit();
			con.close();
			db.getEntityFactory().getCache().evictAll();

		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to create provider links", e);
		}
	}

	private Map<Long, Map<Integer, Long>> buildLinkMap(
			IDatabase db) throws Exception {
		String sql = "SELECT id, ref_id FROM tbl_processes";
		HashMap<String, Long> processMap = new HashMap<>();
		NativeSql.on(db).query(sql, r -> {
			processMap.put(r.getString(2), r.getLong(1));
			return true;
		});
		Map<Long, Map<Integer, Long>> map = new HashMap<>();
		Logger log = null;
		for (Link link : links) {
			Long processID = processMap.get(link.process);
			Long providerID = processMap.get(link.provider);
			if (processID == null || providerID == null) {
				if (log == null) {
					// we only log this once as this problem
					// could occur many many times
					log = LoggerFactory.getLogger(getClass());
					log.warn("Could not set all provider links, " +
							"as not all processes exist; e.g. in {}", link);
				}
				continue;
			}
			Map<Integer, Long> imap = map.get(processID);
			if (imap == null) {
				imap = new HashMap<Integer, Long>();
				map.put(processID, imap);
			}
			imap.put(link.exchange, providerID);
		}
		return map;
	}
}
