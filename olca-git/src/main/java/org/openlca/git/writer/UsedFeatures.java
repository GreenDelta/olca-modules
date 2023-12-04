package org.openlca.git.writer;

import java.util.List;

import org.openlca.git.RepositoryInfo;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.jsonld.LibraryLink;

/**
 * Class is used to track features used in a commit. Depending on used features,
 * the repository client and server versions that are (fully) compatible are
 * determined
 */
class UsedFeatures {

	private final RepositoryInfo previous;

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

	static UsedFeatures of(OlcaRepository repo) {
		return new UsedFeatures(repo.getInfo());
	}

	void emptyCategories() {
		this.emptyCategories = true;
	}

	RepositoryInfo createInfo(List<LibraryLink> libraries) {
		if (didUnmountLibrary(libraries)) {
			unmountLibrary = true;
		}
		return RepositoryInfo.create()
				.withLibraries(libraries)
				.withRepositoryClientVersion(getClientVersion())
				.withRepositoryServerVersion(getServerVersion());
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
		if (emptyCategories || unmountLibrary)
			return max(previousClient, 3);
		return max(previousClient, 2);
	}

	private int getServerVersion() {
		var previousServer = previous != null ? previous.repositoryServerVersion() : 2;
		if (emptyCategories)
			return max(previousServer, 3);
		return max(previousServer, 2);
	}

	private int max(int v1, int v2) {
		return v1 > v2 ? v1 : v2;
	}

}