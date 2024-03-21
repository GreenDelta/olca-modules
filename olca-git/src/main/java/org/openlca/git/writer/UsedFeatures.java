package org.openlca.git.writer;

import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.LibraryLink;
import org.openlca.jsonld.PackageInfo;
import org.openlca.jsonld.SchemaVersion;

import com.google.gson.JsonObject;

/**
 * Class is used to track features used in a commit. Depending on used features,
 * the repository client and server versions that are (fully) compatible are
 * determined
 */
public class UsedFeatures {

	private final RepositoryInfo previous;

	/**
	 * Repository contains additional process documentation fields that were
	 * added in schema version 3 (client: 4, server: 4)
	 */
	private boolean schemaVersion3;

	/**
	 * Repository contains empty categories (client: 3, server: 3)
	 */
	private boolean emptyCategories;

	/**
	 * Libraries were unmounted (client: 3, server: 2)
	 */
	private boolean unmountLibrary;

	private UsedFeatures(RepositoryInfo previous) {
		this.previous = previous;
	}

	public static UsedFeatures of(PackageInfo packageInfo) {
		var usedFeatures = new UsedFeatures(null);
		if (packageInfo != null && packageInfo.schemaVersion().value() == 3) {
			usedFeatures.schemaVersion3 = true;
		}
		return usedFeatures;
	}

	static UsedFeatures of(OlcaRepository repo) {
		return new UsedFeatures(repo.getInfo());
	}

	static UsedFeatures of(OlcaRepository repo, ObjectId[] commitIds) {
		RepositoryInfo mergedInfo = null;
		for (var commitId : commitIds) {
			var commit = repo.commits.get(commitId.getName());
			var info = repo.getInfo(commit);
			if (mergedInfo == null) {
				mergedInfo = info;
				continue;
			}
			mergedInfo = mergedInfo.merge(info);
		}
		return new UsedFeatures(mergedInfo);
	}

	void emptyCategories() {
		this.emptyCategories = true;
	}

	void isSchemaVersion3(JsonObject o) {
		if (this.schemaVersion3)
			return;
		var type = Json.getString(o, "@type");
		if (type == null || !type.equals(Process.class.getSimpleName()))
			return;
		var doc = Json.getObject(o, "processDocumentation");
		if (doc == null)
			return;
		if (!doc.has("flowCompleteness")
				&& !doc.has("reviews")
				&& !doc.has("complianceDeclarations")
				&& !doc.has("useAdvice"))
			return;
		this.schemaVersion3 = true;
	}

	RepositoryInfo createInfo(List<LibraryLink> libraries) {
		if (didUnmountLibrary(libraries)) {
			unmountLibrary = true;
		}
		var info = RepositoryInfo.create()
				.withLibraries(libraries)
				.withRepositoryClientVersion(getClientVersion())
				.withRepositoryServerVersion(getServerVersion());
		if (!schemaVersion3) {
			info = info.withSchemaVersion(new SchemaVersion(2));
		}
		return info;
	}

	private boolean didUnmountLibrary(List<LibraryLink> libraries) {
		if (previous == null)
			return false;
		var libraryIds = libraries.stream().map(LibraryLink::id).toList();
		for (var lib : previous.libraries())
			if (!libraryIds.contains(lib.id()))
				return true;
		return false;
	}

	private int getClientVersion() {
		var previousClient = previous != null ? previous.repositoryClientVersion() : 2;
		if (schemaVersion3)
			return max(previousClient, 4);
		if (emptyCategories || unmountLibrary)
			return max(previousClient, 3);
		return max(previousClient, 2);
	}

	private int getServerVersion() {
		var previousServer = previous != null ? previous.repositoryServerVersion() : 2;
		if (schemaVersion3)
			return max(previousServer, 4);
		if (emptyCategories)
			return max(previousServer, 3);
		return max(previousServer, 2);
	}

	private int max(int v1, int v2) {
		return v1 > v2 ? v1 : v2;
	}

}