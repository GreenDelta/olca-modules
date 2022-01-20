package org.openlca.git;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.database.IDatabase;

public class Config {

	public final IDatabase database;
	public final ObjectIdStore store;
	public final FileRepository repo;
	public final PersonIdent committer;
	public final boolean asProto;
	public boolean checkExisting = true;
	public int converterThreads = 50;

	private Config(IDatabase db, ObjectIdStore store, FileRepository repo, PersonIdent committer, boolean asProto) {
		this.database = db;
		this.repo = repo;
		this.committer = committer;
		this.asProto = asProto;
		this.store = store;
	}

	public static Config newJsonConfig(IDatabase db, ObjectIdStore store, FileRepository repo, PersonIdent committer) {
		return new Config(db, store, repo, committer, false);
	}

	public static Config newProtoConfig(IDatabase db, ObjectIdStore store, FileRepository repo, PersonIdent committer) {
		return new Config(db, store, repo, committer, true);
	}

}