package org.openlca.io.olca;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;

public class TestTransferContext {

	private static final IDatabase source = Derby.createInMemory();
	private static final IDatabase target = Derby.createInMemory();

	public static TestTransferContext get() {
		return new TestTransferContext();
	}

	public IDatabase source() {
		return source;
	}

	public IDatabase target() {
		return target;
	}

	public void clear() {
		source.clear();
		target.clear();
	}
}
