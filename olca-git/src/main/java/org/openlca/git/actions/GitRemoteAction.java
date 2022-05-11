package org.openlca.git.actions;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.CredentialsProvider;

public abstract class GitRemoteAction<T> {

	protected ProgressMonitor monitor;
	protected CredentialsProvider credentialsProvider;

	public GitRemoteAction<T> withProgress(ProgressMonitor monitor) {
		this.monitor = monitor;
		return this;
	}

	public GitRemoteAction<T> authorizeWith(CredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
		return this;
	}

	public abstract T run() throws GitAPIException;

}