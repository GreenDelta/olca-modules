package org.openlca.git;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.openlca.git.util.Repositories;
import org.openlca.jsonld.SchemaVersion;

public class Compatibility {

	public static void check(Repository repo) throws IOException {
		var previousCommit = Repositories.headCommitOf(repo);
		if (previousCommit == null)
			return;
		var info = Repositories.infoOf(repo);
		if (info == null)
			throw new IOException("Info is missing in repository");
		checkSchemaVersion(info);
		checkRepositoryVersion(info);
	}

	private static void checkSchemaVersion(RepositoryInfo info) throws IOException {
		var schema = info.schemaVersion();
		var version = schema != null ? schema.value() : SchemaVersion.fallback().value();
		if (version != SchemaVersion.current().value())
			throw new IOException("Unsupported schema version: " + version);
	}

	private static void checkRepositoryVersion(RepositoryInfo info) throws IOException {
		var repoVersion = info.repositoryVersion();
		var version = repoVersion != null ? repoVersion.value() : RepositoryVersion.fallback().value();
		if (version != RepositoryVersion.current().value())
			throw new IOException("Unsupported repository version: " + version);		
	}

}
