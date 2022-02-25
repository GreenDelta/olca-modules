package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Commit;
import org.openlca.git.util.Constants;

public class GitFetch extends GitRemoteAction<List<Commit>> {

	private final FileRepository git;
	private final Commits commits;

	private GitFetch(FileRepository git) {
		this.git = git;
		this.commits = Commits.of(git);
	}

	public static GitFetch from(FileRepository git) {
		return new GitFetch(git);
	}

	@Override
	public List<Commit> run() throws GitAPIException {
		if (git == null) 
			throw new IllegalStateException("Git repository must be set");
		var lastId = commits.find()
				.refs(Constants.REMOTE_REF)
				.latestId();
		var result = Git.wrap(git).fetch()
				.setCredentialsProvider(credentialsProvider)
				.setRemote(Constants.DEFAULT_REMOTE)
				.setRefSpecs(Constants.DEFAULT_FETCH_SPEC)
				.call();
		if (result == null)
			return new ArrayList<>();
		// TODO check if list is always correct
		var newCommits = commits.find()
				.refs(Constants.REMOTE_REF)
				.after(lastId)
				.all();
		Collections.reverse(newCommits);
		return newCommits;
	}

}
