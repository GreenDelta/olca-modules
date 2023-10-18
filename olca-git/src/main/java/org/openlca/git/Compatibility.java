package org.openlca.git;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.openlca.git.util.Repositories;

public class Compatibility {

	public static void checkRepositoryClientVersion(Repository repo) throws UnsupportedClientVersionException {
		var version = getRepositoryClientVersion(repo);
		if (!RepositoryInfo.REPOSITORY_SUPPORTED_CLIENT_VERSIONS.contains(version))
			throw new UnsupportedClientVersionException(version);
	}

	public static int getRepositoryClientVersion(Repository repo) {
		var head = Repositories.headCommitOf(repo);
		if (head == null)
			return RepositoryInfo.REPOSITORY_CURRENT_CLIENT_VERSION;
		var info = Repositories.infoOf(repo);
		if (info == null)
			return 1;
		return info.repositoryClientVersion();
	}

	public static void checkRepositoryServerVersion(Repository repo) throws UnsupportedServerVersionException {
		var version = getRepositoryServerVersion(repo);
		if (!RepositoryInfo.REPOSITORY_SUPPORTED_SERVER_VERSIONS.contains(version))
			throw new UnsupportedServerVersionException(version);
	}

	public static int getRepositoryServerVersion(Repository repo) {
		var head = Repositories.headCommitOf(repo);
		if (head == null)
			return RepositoryInfo.REPOSITORY_CURRENT_SERVER_VERSION;
		var info = Repositories.infoOf(repo);
		if (info == null)
			return 1;
		return info.repositoryServerVersion();
	}

	public static class UnsupportedClientVersionException extends IOException {

		private static final long serialVersionUID = 3712684307441168749L;
		public final int version;

		private UnsupportedClientVersionException(int version) {
			super("Unsupported repository client version: " + version);
			this.version = version;
		}

	}

	public static class UnsupportedServerVersionException extends IOException {

		private static final long serialVersionUID = -2266619992695144709L;
		public final int version;

		private UnsupportedServerVersionException(int version) {
			super("Unsupported repository server version: " + version);
			this.version = version;
		}

	}

}
