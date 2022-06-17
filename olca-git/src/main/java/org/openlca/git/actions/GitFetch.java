package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Commit;
import org.openlca.git.util.Constants;

public class GitFetch extends GitRemoteAction<List<Commit>> {

	private final Repository repo;
	private final Commits commits;

	private GitFetch(Repository repo) {
		this.repo = repo;
		this.commits = Commits.of(repo);
	}

	public static GitFetch to(Repository repo) {
		return new GitFetch(repo);
	}

	@Override
	public List<Commit> run() throws GitAPIException {
		if (repo == null) 
			throw new IllegalStateException("Git repository must be set");
		var lastId = commits.find()
				.refs(Constants.REMOTE_REF)
				.latestId();
		var result = Git.wrap(repo).fetch()
				.setCredentialsProvider(credentialsProvider)
				.setRemote(Constants.DEFAULT_REMOTE)
				.setRefSpecs(Constants.DEFAULT_FETCH_SPEC)
				.call();
		if (result == null)
			return new ArrayList<>();
		var newCommits = commits.find()
				.refs(Constants.REMOTE_REF)
				.after(lastId)
				.all();
		Collections.reverse(newCommits);
		return newCommits;
	}

}
