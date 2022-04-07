package org.openlca.git.actions;

import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.openlca.git.actions.GitPush.PushResponse;
import org.openlca.git.model.Commit;
import org.openlca.git.util.Constants;
import org.openlca.git.util.History;

public class GitPush extends GitRemoteAction<PushResponse> {

	private final FileRepository git;
	private final History history;
	
	private GitPush(FileRepository git) {
		this.git = git;
		this.history = History.of(git);
	}

	public static GitPush to(FileRepository git) {
		return new GitPush(git);
	}

	@Override
	public PushResponse run() throws GitAPIException {
		if (git == null) 
			throw new IllegalStateException("Git repository must be set");
		var newCommits = history.getAhead();
		if (newCommits.isEmpty())
			return new PushResponse(newCommits, Status.NOT_ATTEMPTED);
		Git.wrap(git).gc().call();
		var result = Git.wrap(git).push()
				.setCredentialsProvider(credentialsProvider)
				.setProgressMonitor(monitor)
				.setRemote(Constants.DEFAULT_REMOTE)
				.setRefSpecs(new RefSpec(Constants.LOCAL_REF))
				.call();
		var update = getUpdate(result);
		if (update == null)
			return new PushResponse(newCommits, null);
		return new PushResponse(newCommits, update.getStatus());
	}

	private RemoteRefUpdate getUpdate(Iterable<PushResult> results) {
		if (results == null)
			return null;
		var iterator = results.iterator();
		if (!iterator.hasNext())
			return null;
		var result = iterator.next();
		if (result == null)
			return null;
		return result.getRemoteUpdate(Constants.LOCAL_REF);
	}

	public record PushResponse(List<Commit> newCommits, Status status) {
	}

}
