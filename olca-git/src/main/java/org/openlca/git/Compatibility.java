package org.openlca.git;

import java.io.IOException;

import org.openlca.git.repo.OlcaRepository;

public class Compatibility {

	public static void checkRepositoryClientVersion(OlcaRepository repo) throws UnsupportedClientVersionException {
		var version = getRepositoryClientVersion(repo);
		if (!RepositoryInfo.REPOSITORY_SUPPORTED_CLIENT_VERSIONS.contains(version))
			throw new UnsupportedClientVersionException(version);
	}

	public static int getRepositoryClientVersion(OlcaRepository repo) {
		var head = repo.getHeadCommit();
		if (head == null)
			return RepositoryInfo.REPOSITORY_CURRENT_CLIENT_VERSION;
		var info = repo.getInfo();
		if (info == null)
			return RepositoryInfo.REPOSITORY_CLIENT_VERSION_FALLBACK;
		return info.repositoryClientVersion();
	}

	public static void checkRepositoryServerVersion(OlcaRepository repo) throws UnsupportedServerVersionException {
		var version = getRepositoryServerVersion(repo);
		if (!RepositoryInfo.REPOSITORY_SUPPORTED_SERVER_VERSIONS.contains(version))
			throw new UnsupportedServerVersionException(version);
	}

	public static int getRepositoryServerVersion(OlcaRepository repo) {
		var head = repo.getHeadCommit();
		if (head == null)
			return RepositoryInfo.REPOSITORY_CURRENT_SERVER_VERSION;
		var info = repo.getInfo();
		if (info == null)
			return RepositoryInfo.REPOSITORY_SERVER_VERSION_FALLBACK;
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
