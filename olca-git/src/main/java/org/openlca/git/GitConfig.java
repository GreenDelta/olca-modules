package org.openlca.git;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.openlca.core.database.IDatabase;

public class GitConfig {

	public final IDatabase database;
	public final ObjectIdStore store;
	public final FileRepository repo;
	public boolean checkExisting = true;
	public int converterThreads = 50;

	public GitConfig(IDatabase db, ObjectIdStore store, FileRepository repo) {
		this.database = db;
		this.repo = repo;
		this.store = store;
	}

}