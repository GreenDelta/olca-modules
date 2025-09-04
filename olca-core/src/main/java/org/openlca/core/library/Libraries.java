package org.openlca.core.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Exchanges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Libraries {

	private static final Logger log = LoggerFactory.getLogger(Libraries.class);

	private Libraries() {
	}

	/**
	 * Returns the dependencies of the given library in topological order. The
	 * returned list contains the given library itself at the last position as
	 * no cycles are allowed in a library dependency graph.
	 */
	public static List<Library> dependencyOrderOf(Library lib) {
		if (lib == null)
			return Collections.emptyList();

		var stack = new Stack<Library>();
		var queue = new ArrayDeque<Library>();
		queue.add(lib);
		while (!queue.isEmpty()) {
			var next = queue.poll();
			stack.push(next);
			queue.addAll(next.getDirectDependencies());
		}

		var handled = new HashSet<Library>();
		var order = new ArrayList<Library>();
		while (!stack.isEmpty()) {
			var next = stack.pop();
			if (!handled.contains(next)) {
				order.add(next);
				handled.add(next);
			}
		}
		return order;
	}

	/// Adds all exchanges to the given process. A library process in the
	/// database
	/// only contains its provider flow (the product output or waste input of
	/// that
	/// process). This method adds all other exchanges to the process too. This
	/// is
	/// useful when the process should be displayed in the database or when it
	/// is
	/// converted into a non-library process. Note that this method does not
	/// update the process in the database.
	public static void fillExchangesOf(
			IDatabase db, LibReader lib, Process process) {
		if (db == null || lib == null || process == null)
			return;
		var techFlow = TechFlow.of(process);

		// add the exchanges, ignoring provider flows
		var exchanges = lib.getExchanges(techFlow, db);
		int iid = Math.max(process.lastInternalId, 1);
		for (var e : exchanges) {
			if (Exchanges.isProviderFlow(e))
				continue;
			iid++;
			e.internalId = iid;
			process.exchanges.add(e);
		}
		process.lastInternalId = iid;

		// also, add the net costs to the quant. ref. if applicable
		// "costs" for provider flows mean added value; so we have
		// to invert the value
		var qRef = process.quantitativeReference;
		if (Exchanges.isProviderFlow(qRef)) {
			var costs = getNetCosts(lib, techFlow);
			if (costs != 0) {
				qRef.costs = -costs;
				qRef.currency = new CurrencyDao(db).getReferenceCurrency();
			}
		}
	}

	/// Adds all impact factors to the given impact category. This does not
	/// update
	/// the impact category in the database.
	public static void fillFactorsOf(
			IDatabase db, LibReader lib, ImpactCategory impact) {
		if (db == null || lib == null || impact == null)
			return;
		var factors = lib.getImpactFactors(Descriptor.of(impact), db);
		impact.impactFactors.addAll(factors);
	}

	private static double getNetCosts(LibReader lib, TechFlow techFlow) {
		if (!lib.hasCostData())
			return 0;
		var costs = lib.costs();
		if (costs == null)
			return 0;
		int i = lib.techIndex().of(techFlow);
		return i < 0 ? 0 : costs[i];
	}

	public static Library importFromFile(File file, LibraryDir libDir) throws NoValidLibraryPackageException {
		if (file == null)
			return null;
		var info = LibraryPackage.getInfo(file);
		if (info == null)
			throw new NoValidLibraryPackageException();
		LibraryPackage.unzip(file, libDir);
		return libDir.getLibrary(info.name()).orElse(null);
	}

	public static Library importFromUrl(String url, LibraryDir libDir) throws NoValidLibraryUrlException, NoValidLibraryPackageException {
		try {
			var encoded = encodeUrl(url);
			try (var stream = URI.create(encoded).toURL().openStream()) {
				return importFromStream(stream, libDir);
			}
		} catch (IOException e) {
			throw new NoValidLibraryUrlException();
		}
	}

	public static Library importFromStream(InputStream stream, LibraryDir libDir) throws NoValidLibraryPackageException {
		var file = (Path) null;
		var library = (Library) null;
		try {
			file = Files.createTempFile("olca-library", ".zip");
			Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
			library = importFromFile(file.toFile(), libDir);
			return library;
		} catch (IOException e) {
			log.error("Error copying library from stream", e);
			return null;
		} finally {
			if (file != null && file.toFile().exists()) {
				try {
					Files.delete(file);
				} catch (IOException e) {
					log.trace("Error deleting tmp file", e);
				}
			}
		}
	}

	/// Library names can contain spaces, that we need to encode. Also, the
	/// standard URL encoding does not encode spaces as %20, but as +, which
	/// seem to cannot be handled by the Collaboration Server?
	private static String encodeUrl(String url) {
		if (url == null)
			return null;
		var parts = url.split("://");
		if (parts.length < 2)
			return url.strip();
		var protocol = parts[0];
		var sub = parts[1].split("/");
		if (sub.length < 2)
			return url.strip();
		var encoded = new StringBuilder(protocol.strip())
				.append("://")
				.append(sub[0].strip());
		for (int i = 1; i < sub.length; i++) {
			var segment = URLEncoder.encode(sub[i].strip(), StandardCharsets.UTF_8)
					.replace("+", "%20");
			encoded.append("/").append(segment);
		}
		return encoded.toString();
	}

	public static class NoValidLibraryPackageException extends Exception {

		private static final long serialVersionUID = 3534858021820406145L;

	}

	public static class NoValidLibraryUrlException extends Exception {

		private static final long serialVersionUID = 3534858021820406145L;

	}

}
