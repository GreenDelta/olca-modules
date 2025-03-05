package org.openlca.git.writer;

import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.database.IDatabase.DataPackage;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ProductSystem;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.jsonld.Json;
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
	 * added in <br>
	 * schema version 3 (client: 4, server: 4) or <br>
	 * schema version 4 (client: 5, server: 5) schema version 5 (client: 6,
	 * server: 5)
	 */
	private int schemaVersion = 2;

	/**
	 * Has packages, that are not legacy libraries (only name) (client: 7,
	 * server: 6)
	 */
	private boolean usesPackageFormat;

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
		if (packageInfo != null && packageInfo.schemaVersion().value() >= 3) {
			usedFeatures.schemaVersion = 3;
		}
		if (packageInfo != null && packageInfo.schemaVersion().value() >= 4) {
			usedFeatures.schemaVersion = 4;
		}
		if (packageInfo != null && packageInfo.schemaVersion().value() >= 5) {
			usedFeatures.schemaVersion = 5;
		}
		return usedFeatures;
	}

	static UsedFeatures of(OlcaRepository repo) {
		return new UsedFeatures(repo.getInfo());
	}

	static UsedFeatures of(OlcaRepository repo, ObjectId[] commitIds) {
		RepositoryInfo mergedInfo = null;
		for (var commitId : commitIds) {
			var info = repo.getInfo(commitId.getName());
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

	private boolean isSchemaVersion5(JsonObject o) {
		if (schemaVersion == 5)
			return true;
		var type = Json.getString(o, "@type");
		if (type == null || !type.equals(ProductSystem.class.getSimpleName()))
			return false;
		if (!hasAnyField(o, "analysisGroups"))
			return false;
		return true;
	}

	private boolean isSchemaVersion4(JsonObject o) {
		if (schemaVersion == 4)
			return true;
		var type = Json.getString(o, "@type");
		if (type == null || !type.equals(Epd.class.getSimpleName()))
			return false;
		if (!hasAnyField(o, "epdType", "validFrom", "validUntil", "location", "originalEpd", "manufacturing",
				"productUsage", "useAdvice", "registrationId", "dataGenerator"))
			return false;
		return true;
	}

	private boolean isSchemaVersion3(JsonObject o) {
		if (schemaVersion == 3)
			return true;
		var type = Json.getString(o, "@type");
		if (type == null || !type.equals(Process.class.getSimpleName()))
			return false;
		var doc = Json.getObject(o, "processDocumentation");
		if (!hasAnyField(doc, "flowCompleteness", "reviews", "complianceDeclarations", "useAdvice"))
			return false;
		return true;
	}

	void checkSchemaVersion(JsonObject o) {
		if (isSchemaVersion5(o)) {
			schemaVersion = 5;
		} else if (isSchemaVersion4(o)) {
			schemaVersion = 4;
		} else if (isSchemaVersion3(o)) {
			schemaVersion = 3;
		}
	}

	private boolean hasAnyField(JsonObject o, String... fields) {
		if (o == null || fields == null)
			return false;
		for (var field : fields)
			if (o.has(field))
				return true;
		return false;
	}

	RepositoryInfo createInfo(Set<DataPackage> packages) {
		if (usesPackageFormat(packages)) {
			usesPackageFormat = true;
		} else if (didUnmountLibrary(packages)) {
			unmountLibrary = true;
		}
		var info = RepositoryInfo.create()
				.withDataPackages(packages)
				.withSchemaVersion(new SchemaVersion(schemaVersion))
				.withRepositoryClientVersion(getClientVersion())
				.withRepositoryServerVersion(getServerVersion());
		return info;
	}

	private boolean usesPackageFormat(Set<DataPackage> packages) {
		if (packages == null || packages.isEmpty())
			return false;
		for (var p : packages)
			if (isPackageFormat(p))
				return true;
		return false;
	}

	private boolean didUnmountLibrary(Set<DataPackage> packages) {
		if (previous == null || previous.dataPackages().isEmpty())
			return false;
		if (packages == null || packages.isEmpty())
			return true;
		var libraryIds = packages.stream()
				.filter(Predicate.not(this::isPackageFormat))
				.map(p -> p.name()).toList();
		for (var p : previous.dataPackages())
			if (!isPackageFormat(p) && !libraryIds.contains(p.name()))
				return true;
		return false;
	}
	
	private boolean isPackageFormat(DataPackage p) {
		return !p.isLibrary() || p.version().getValue() != 0l;
	}

	private int getClientVersion() {
		var previousClient = previous != null ? previous.repositoryClientVersion() : 2;
		if (usesPackageFormat)
			return max(previousClient, 7);
		if (schemaVersion == 5)
			return max(previousClient, 6);
		if (schemaVersion == 4)
			return max(previousClient, 5);
		if (schemaVersion == 3)
			return max(previousClient, 4);
		if (emptyCategories || unmountLibrary)
			return max(previousClient, 3);
		return max(previousClient, 2);
	}

	private int getServerVersion() {
		var previousServer = previous != null ? previous.repositoryServerVersion() : 2;
		if (usesPackageFormat)
			return max(previousServer, 6);
		if (schemaVersion == 4 || schemaVersion == 5)
			return max(previousServer, 5);
		if (schemaVersion == 3)
			return max(previousServer, 4);
		if (emptyCategories)
			return max(previousServer, 3);
		return max(previousServer, 2);
	}

	private int max(int v1, int v2) {
		return v1 > v2 ? v1 : v2;
	}

}