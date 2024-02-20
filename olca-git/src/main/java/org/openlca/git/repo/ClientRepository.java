package org.openlca.git.repo;

import java.io.File;
import java.io.IOException;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;

public class ClientRepository extends OlcaRepository {

	public final HeadIndex index;
	public final IDatabase database;
	public final FileStore fileStore;
	public final Descriptors descriptors;

	public ClientRepository(File gitDir, IDatabase database) throws IOException {
		super(gitDir);
		this.index = HeadIndex.of(this);
		this.database = database;
		this.fileStore = new FileStore(database);
		this.descriptors = Descriptors.of(database);
	}

}
