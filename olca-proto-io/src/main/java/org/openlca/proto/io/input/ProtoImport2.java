package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.CategorySync;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.input.ImportCache;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.io.ProtoStoreReader;

public class ProtoImport2 implements Runnable, EntityResolver {

	private final IDatabase db;
	final ProtoStoreReader reader;
	UpdateMode updateMode = UpdateMode.NEVER;
	final CategorySync categories;
	private final ImportCache cache = new ImportCache(this);
	final Map<Class<?>, ModelType> types = new HashMap<>();
}
