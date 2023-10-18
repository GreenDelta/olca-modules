package org.openlca.git;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.openlca.git.util.Repositories;

public class Compatibility {

	public static void checkClient(Repository repo) throws UnsupportedClientVersionException {
		var previousCommit = Repositories.headCommitOf(repo);
		if (previousCommit == null)
			return;
		var info = Repositories.infoOf(repo);
		checkRepositoryClientVersion(info);
	}

	public static void checkServer(Repository repo) throws UnsupportedServerVersionException {
		var previousCommit = Repositories.headCommitOf(repo);
		if (previousCommit == null)
			return;
		var info = Repositories.infoOf(repo);
		checkRepositoryServerVersion(info);
	}

	private static void checkRepositoryClientVersion(RepositoryInfo info) throws UnsupportedClientVersionException {
		var version = info != null ? info.repositoryClientVersion() : 1;
		if (!RepositoryInfo.REPOSITORY_SUPPORTED_CLIENT_VERSIONS.contains(version))
			throw new UnsupportedClientVersionException(version);
	}

	private static void checkRepositoryServerVersion(RepositoryInfo info) throws UnsupportedServerVersionException {
		var version = info != null ? info.repositoryServerVersion() : 1;
		if (!RepositoryInfo.REPOSITORY_SUPPORTED_SERVER_VERSIONS.contains(version))
			throw new UnsupportedServerVersionException(version);
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
