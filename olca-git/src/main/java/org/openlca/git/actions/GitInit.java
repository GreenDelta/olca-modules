package org.openlca.git.actions;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.openlca.git.util.Constants;

public class GitInit {

	private File gitDir;
	private String remoteUrl;

	private GitInit(File gitDir) {
		this.gitDir = gitDir;
	}

	public static GitInit in(File gitDir) {
		return new GitInit(gitDir);
	}

	public GitInit remoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
		return this;
	}

	public void run() throws GitAPIException, URISyntaxException {
		var git = Git.init()
				.setInitialBranch(Constants.DEFAULT_BRANCH)
				.setBare(true)
				.setGitDir(gitDir)
				.call();
		git.remoteAdd()
				.setName(Constants.DEFAULT_REMOTE)
				.setUri(new URIish(remoteUrl))
				.call();
	}

}
