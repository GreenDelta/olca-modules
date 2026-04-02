package org.openlca.io.olca;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;

class TestContext {

	private static final IDatabase source = Derby.createInMemory();
	private static final IDatabase target = Derby.createInMemory();

	static TestContext get() {
		return new TestContext();
	}

	IDatabase source() {
		return source;
	}

	IDatabase target() {
		return target;
	}

	void clear() {
		source.clear();
		target.clear();
	}
}
