package org.openlca.git;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.database.IDatabase;

public class Config {

	public final IDatabase database;
	public final ObjectIdStore store;
	public final FileRepository repo;
	public final PersonIdent committer;
	public boolean checkExisting = true;
	public int converterThreads = 50;

	public Config(IDatabase db, ObjectIdStore store, FileRepository repo, PersonIdent committer) {
		this.database = db;
		this.repo = repo;
		this.committer = committer;
		this.store = store;
	}

}