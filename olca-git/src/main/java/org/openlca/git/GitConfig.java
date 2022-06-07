package org.openlca.git;

import org.eclipse.jgit.lib.Repository;
import org.openlca.core.database.IDatabase;

public class GitConfig {

	public final IDatabase database;
	public final ObjectIdStore store;
	public final Repository repo;
	public boolean checkExisting = true;
	public int converterThreads = 50;

	public GitConfig(IDatabase db, ObjectIdStore store, Repository repo) {
		this.database = db;
		this.repo = repo;
		this.store = store;
	}

}