package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.CategorySync;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.io.ProtoStoreReader;

public class ProtoImport implements Runnable, EntityResolver {

	private final IDatabase db;
	final ProtoStoreReader reader;
	UpdateMode updateMode = UpdateMode.NEVER;
	final CategorySync categories;
	final Map<Class<?>, ModelType> types = new HashMap<>();

	private final ImportCache cache = new ImportCache(this);
	private final ExchangeProviderQueue providers;

	public ProtoImport(ProtoStoreReader reader, IDatabase db) {
		this.db = db;
		this.reader = reader;
		this.providers = ExchangeProviderQueue.create(db);
		this.categories = CategorySync.of(db);
		for (var type : ModelType.values()) {
			if (type.isRoot()) {
				types.put(type.getModelClass(), type);
			}
		}
	}

	public ProtoImport setUpdateMode(UpdateMode mode) {
		this.updateMode = mode;
		return this;
	}

	@Override
	public IDatabase db() {
		return db;
	}

	void visited(RootEntity entity) {
		if (entity instanceof Process p) {
			providers.pop(p);
		}
		cache.visited(entity);
	}

	@SuppressWarnings("unchecked")
	public RootEntity run(ModelType type, String id) {
		if (type == null || !type.isRoot() || id == null)
			return null;
		var clazz = type.getModelClass();
		return clazz != null
			? get((Class<? extends RootEntity>) clazz, id)
			: null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		new UnitGroupImport(this).importAll();
		var typeOrder = new ModelType[]{
			ModelType.ACTOR,
			ModelType.SOURCE,
			ModelType.CURRENCY,
			ModelType.DQ_SYSTEM,
			ModelType.LOCATION,
			ModelType.FLOW_PROPERTY,
			ModelType.FLOW,
			ModelType.SOCIAL_INDICATOR,
			ModelType.PARAMETER,
			ModelType.PROCESS,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.PRODUCT_SYSTEM,
			ModelType.PROJECT,
			ModelType.RESULT,
			ModelType.EPD,
		};
		for (var type : typeOrder) {
			var batchSize = BatchImport.batchSizeOf(type);
			if (batchSize > 1) {
				var clazz = (Class<? extends RootEntity>) type.getModelClass();
				new BatchImport<>(this, clazz, batchSize).run();
			} else {
				for (var id : reader.getIds(type)) {
					run(type, id);
				}
			}
		}
	}

	@Override
	public <T extends RootEntity> T get(Class<T> type, String refId) {
		// unit groups can have cyclic dependencies with flow properties
		// thus, we handle them a bit differently than other types
		if (Objects.equals(UnitGroup.class, type))
			return new UnitGroupImport(this).get(type, refId);

		var item = cache.fetch(type, refId);
		if (item.isError() || item.proto() == null)
			return null;
		if (item.isVisited())
			return item.entity();

		var model = item.entity();

		if (model == null) {
			model = item.proto().read(this);
			if (model == null)
				return null;
			db.insert(model);
		} else {
			item.proto().update(model, this);
			db.update(model);
		}

		visited(model);

		return model;
	}

	@Override
	public <T extends RootEntity> Descriptor getDescriptor(
		Class<T> type, String refId) {
		var d = cache.getDescriptor(type, refId);
		if (d != null)
			return d;
		var model = get(type, refId);
		return model != null
			? Descriptor.of(model)
			: null;
	}

	<T extends RootEntity> ImportItem<T> fetch(Class<T> type, String refId) {
		return cache.fetch(type, refId);
	}

	@Override
	public Category getCategory(ModelType type, String path) {
		return categories.get(type, path);
	}

	@Override
	public void resolveProvider(String providerId, Exchange exchange) {
		providers.add(providerId, exchange);
	}
}
