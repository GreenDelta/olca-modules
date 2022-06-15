package org.openlca.git.actions;

import java.io.IOException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

public class GitStashDrop {

	private final Repository repo;

	private GitStashDrop(Repository repo) {
		this.repo = repo;
	}

	public static GitStashDrop from(Repository repo) {
		return new GitStashDrop(repo);
	}

	public void run() throws IOException {
		if (repo == null)
			throw new IllegalStateException("Git repository must be set");
		var update = repo.updateRef(Constants.R_STASH);
		update.disableRefLog();
		update.setForceUpdate(true);
		update.delete();
	}

}