package org.openlca.git.actions;

import java.io.IOException;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;

public class GitStashDrop {

	private final FileRepository git;

	private GitStashDrop(FileRepository git) {
		this.git = git;
	}

	public static GitStashDrop from(FileRepository git) {
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