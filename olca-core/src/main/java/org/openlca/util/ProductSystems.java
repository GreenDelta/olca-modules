package org.openlca.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.slf4j.LoggerFactory;

public final class ProductSystems {

	private ProductSystems() {
	}

	/**
	 * Returns true when the given product system has links to library
	 * processes.
	 */
	public static boolean hasLibraryLinks(ProductSystem sys, IDatabase db) {
		if (sys == null || db == null)
			return false;
		if (sys.referenceProcess != null
				&& sys.referenceProcess.isFromLibrary())
			return true;
		var processes = new ProcessDao(db).descriptorMap();
		for (var processID : sys.processes) {
			var process = processes.get(processID);
			if (process != null && process.isFromLibrary())
				return true;
		}
		return false;
	}

	/**
	 * Removes unreachable processes and process links from the given system. It
	 * traverses the links of the system starting from the reference process and
	 * removes all links and processes that cannot be reached by this traversal.
	 */
	public static void tidy(ProductSystem system) {
		var log = LoggerFactory.getLogger(ProductSystems.class);
		log.info("cleanup product system: {}", system);

		if (system.referenceProcess == null) {
			log.error("system has no reference process");
			return;
		}
		long refProcess = system.referenceProcess.id;

		var linkMap = new HashMap<Long, List<ProcessLink>>();
		for (var link : system.processLinks) {
			linkMap.computeIfAbsent(link.processId, pid -> new ArrayList<>())
					.add(link);
		}

		var validProcesses = new HashSet<Long>();
		var validLinks = new ArrayList<ProcessLink>();
		var queue = new ArrayDeque<Long>();
		queue.add(refProcess);
		while (!queue.isEmpty()) {
			var next = queue.poll();
			if (validProcesses.contains(next))
				continue;
			validProcesses.add(next);

			var inLinks = linkMap.get(next);
			if (inLinks == null)
				continue;
			validLinks.addAll(inLinks);
			for (var link : inLinks) {
				var provider = link.providerId;
				if (!validProcesses.contains(provider)
						&& !queue.contains(provider)) {
					queue.add(provider);
				}
			}
		}

		int processRemovals = system.processes.size() - validProcesses.size();
		system.processes.clear();
		system.processes.addAll(validProcesses);
		int linkRemovals = system.processLinks.size() - validLinks.size();
		system.processLinks.clear();
		system.processLinks.addAll(validLinks);
		log.info("removed {} unreachable processes and {} links",
				processRemovals, linkRemovals);
	}

}
