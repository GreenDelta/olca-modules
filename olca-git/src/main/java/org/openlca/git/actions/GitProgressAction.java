package org.openlca.git.actions;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.openlca.git.util.ProgressMonitor;

public abstract class GitProgressAction<T> {

	protected ProgressMonitor progressMonitor = ProgressMonitor.NULL;

	public GitProgressAction<T> withProgress(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor != null ? progressMonitor : ProgressMonitor.NULL;
		return this;
	}

	public abstract T run() throws IOException, GitAPIException;

}
