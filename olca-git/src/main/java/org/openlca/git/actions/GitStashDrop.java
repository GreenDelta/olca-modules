package org.openlca.git.actions;

import java.io.IOException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

public class GitStashDrop {

	private final Repository git;

	private GitStashDrop(Repository git) {
		this.git = git;
	}

	public static GitStashDrop from(Repository git) {
		return new GitStashDrop(git);
	}

	public void run() throws IOException {
		if (git == null)
			throw new IllegalStateException("Git repository must be set");
		var update = git.updateRef(Constants.R_STASH);
		update.disableRefLog();
		update.setForceUpdate(true);
		update.delete();
	}

}