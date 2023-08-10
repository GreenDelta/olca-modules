package org.openlca.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import gnu.trove.set.hash.TIntHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class Processes {

	private Processes() {
	}

	/**
	 * Get the product outputs or waste inputs of the give process.
	 */
	public static List<Exchange> getProviderFlows(Process p) {
		return p == null
				? Collections.emptyList()
				: p.exchanges.stream()
				.filter(Exchanges::isProviderFlow)
				.collect(Collectors.toList());
	}

	/**
	 * A multi-functional process has multiple product outputs or waste inputs.
	 */
	public static boolean isMultiFunctional(Process p) {
		if (p == null)
			return false;
		int count = 0;
		for (var exchange : p.exchanges) {
			if (Exchanges.isProviderFlow(exchange)) {
				count++;
				if (count > 1)
					return true;
			}
		}
		return false;
	}

	/**
	 * Searches the given database for a process with the given label. A label
	 * can contain a suffix with a location code which is considered in this
	 * function.
	 */
	public static ProcessDescriptor findForLabel(IDatabase db, String label) {
		if (db == null || label == null)
			return null;
		String name = null;
		Location location = null;
		if (label.contains(" - ")) {
			int splitIdx = label.lastIndexOf(" - ");
			name = label.substring(0, splitIdx).trim();
			String locationCode = label.substring(splitIdx + 3).trim();
			LocationDao dao = new LocationDao(db);
			for (Location loc : dao.getAll()) {
				if (Strings.nullOrEqual(loc.code, locationCode)) {
					location = loc;
					break;
				}
			}
		}

		ProcessDescriptor selected = null;
		ProcessDao pDao = new ProcessDao(db);
		for (ProcessDescriptor d : pDao.getDescriptors()) {
			if (!Strings.nullOrEqual(label, d.name)
					&& !Strings.nullOrEqual(name, d.name))
				continue;
			if (selected == null) {
				selected = d;
				if (matchLocation(selected, location))
					break;
				else
					continue;
			}
			if (matchLocation(d, location)
					&& !matchLocation(selected, location)) {
				selected = d;
				break;
			}
		}
		return selected;
	}

	private static boolean matchLocation(ProcessDescriptor d, Location loc) {
		if (d == null)
			return false;
		if (d.location == null)
			return loc == null;
		if (loc == null)
			return false;
		return d.location == loc.id;
	}

	/**
	 * Checks if the process contains a valid sequence of internal IDs and a
	 * valid last-internal ID. Fixes that sequence if there is an issue, does
	 * nothing otherwise.
	 */
	public static void fixInternalIds(Process process) {
		if (process == null)
			return;

		// check exchange IDs
		int last = 0;
		var used = new TIntHashSet();
		boolean shouldFix = false;
		for (var e : process.exchanges) {
			last = Math.max(last, e.internalId);
			if (shouldFix)
				continue;
			if (e.internalId <= 0 || used.contains(e.internalId)) {
				shouldFix = true;
				continue;
			}
			used.add(e.internalId);
		}

		// if there is an issue, we just start with a fresh sequence
		if (shouldFix) {
			for (var e : process.exchanges) {
				e.internalId = ++last;
			}
		}
		if (process.lastInternalId < last) {
			process.lastInternalId = last;
		}
	}
}
