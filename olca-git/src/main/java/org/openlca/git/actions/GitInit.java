package org.openlca.git.actions;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.openlca.git.util.Constants;

public class GitInit {

	private File gitDir;
	private URIish remoteUrl;

	private GitInit(File gitDir) {
		this.gitDir = gitDir;
	}

	public static GitInit in(File gitDir) {
		return new GitInit(gitDir);
	}

	public GitInit remoteUrl(String remoteUrl) throws URISyntaxException {
		this.remoteUrl = new URIish(remoteUrl);
		return this;
	}

	public void run() throws GitAPIException {
		if (gitDir == null)
			throw new IllegalStateException("Git directory must be set");
		try (var git = Git.init()
				.setInitialBranch(Constants.DEFAULT_BRANCH)
				.setBare(true)
				.setGitDir(gitDir)
				.call()) {
			if (remoteUrl == null)
				return;
			git.remoteAdd()
					.setName(Constants.DEFAULT_REMOTE)
					.setUri(remoteUrl)
					.call();
		}
	}

}
