package org.openlca.jsonld.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.CategorySync;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.jsonld.upgrades.Upgrades;
import org.openlca.util.Dirs;
import org.slf4j.LoggerFactory;

public class JsonImport implements Runnable, EntityResolver {

	private final IDatabase db;
	final JsonStoreReader reader;
	UpdateMode updateMode = UpdateMode.NEVER;
	private Consumer<RefEntity> callback;
	final CategorySync categories;

	private final ExchangeProviderQueue providers;
	private final Map<Class<?>, ModelType> types = new HashMap<>();
	private final Map<ModelType, Set<String>> visited = new HashMap<>();

	public JsonImport(JsonStoreReader reader, IDatabase db) {
		this.db = db;
		this.reader = Upgrades.chain(reader);
		this.providers = ExchangeProviderQueue.create(db);
		this.categories = CategorySync.of(db);
		for (var type : ModelType.values()) {
			if (type.isRoot()) {
				types.put(type.getModelClass(), type);
			}
		}
	}

	public JsonImport setUpdateMode(UpdateMode updateMode) {
		this.updateMode = updateMode;
		return this;
	}

	public JsonImport setCallback(Consumer<RefEntity> callback) {
		this.callback = callback;
		return this;
	}

	@Override
	public IDatabase db() {
		return db;
	}

	void visited(ModelType type, String refId) {
		var set = visited.computeIfAbsent(type, k -> new HashSet<>());
		set.add(refId);
	}

	public ExchangeProviderQueue providers() {
		return providers;
	}

	void imported(RefEntity entity) {
		if (callback == null)
			return;
		callback.accept(entity);
	}

	boolean hasVisited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		return set != null && set.contains(refId);
	}

	@SuppressWarnings("unchecked")
	public void run(ModelType type, String id) {
		if (type == null || !type.isRoot() || id == null)
			return;
		var clazz = type.getModelClass();
		if (clazz == null)
			return;
		get((Class<? extends RootEntity>) clazz, id);
	}

	@Override
	public void run() {
		var typeOrder = new ModelType[]{
			ModelType.ACTOR,
			ModelType.SOURCE,
			ModelType.CURRENCY,
			ModelType.DQ_SYSTEM,
			ModelType.LOCATION,
			ModelType.UNIT_GROUP,
			ModelType.FLOW_PROPERTY,
			ModelType.FLOW,
			ModelType.SOCIAL_INDICATOR,
			ModelType.PARAMETER,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.PROCESS,
			ModelType.PRODUCT_SYSTEM,
			ModelType.PROJECT,
			ModelType.RESULT,
			ModelType.EPD,
		};
		for (var type : typeOrder) {
			for (var id : reader.getRefIds(type)) {
				run(type, id);
			}
		}
	}


	@Override
	@SuppressWarnings("unchecked")
	public <T extends RootEntity> T get(Class<T> type, String refId) {
		// unit groups can have cyclic dependencies with flow properties
		// thus, we handle them a bit differently than other types
		if (Objects.equals(UnitGroup.class, type))
			return new UnitGroupImport(this).get(type, refId);

		var item = fetch(type, refId);
		if (item.isError())
			return null;
		if (item.isVisited())
			return item.entity();

		var model = item.entity();
		var modelType = types.get(type);

		var reader = (EntityReader<T>) readerFor(modelType);
		if (reader == null)
			return model;

		if (model == null) {
			model = reader.read(item.json());
			if (model == null)
				return null;
			db.insert(model);
		} else {
			reader.update(model, item.json());
			db.update(model);
		}

		if (model instanceof Process p) {
			providers.pop(p);
		}

		copyBinaryFilesOf(modelType, refId);
		visited(modelType, refId);
		imported(model);

		return model;
	}

	public <T extends RootEntity> ImportItem<T> fetch(
		Class<T> type, String refId) {
		if (type == null || refId == null)
			return ImportItem.error();
		var modelType = types.get(type);
		if (modelType == null)
			return ImportItem.error();
		T model = db.get(type, refId);
		if (model != null) {
			if (hasVisited(modelType, refId))
				return ImportItem.visited(model);
			if (updateMode == UpdateMode.NEVER) {
				visited(modelType, refId);
				return ImportItem.visited(model);
			}
		}
		var json = reader.get(modelType, refId);
		if (json == null) {
			if (model == null)
				return ImportItem.error();
			visited(modelType, refId);
			return ImportItem.visited(model);
		}
		if (skipImport(model, json)) {
			visited(modelType, refId);
			return ImportItem.visited(model);
		}

		return model == null
			? ImportItem.newOf(json)
			: ImportItem.update(json, model);
	}

	private <T extends RefEntity> boolean skipImport(T model, JsonObject json) {
		if (model == null || updateMode == UpdateMode.ALWAYS)
			return false;
		if (!(model instanceof RootEntity root))
			return false;
		long jsonVersion = Util.getVersion(json);
		if (jsonVersion != root.version)
			return jsonVersion < root.version;
		long jsonDate = Util.getLastChange(json);
		return jsonDate <= root.lastChange;
	}

	@Override
	public Category getCategory(ModelType type, String path) {
		return categories.get(type, path);
	}

	@Override
	public void resolveProvider(String providerId, Exchange exchange) {
		providers.add(providerId, exchange);
	}

	private EntityReader<?> readerFor(ModelType type) {
		return switch (type) {
			case ACTOR -> new ActorReader(this);
			case CURRENCY -> new CurrencyReader(this);
			case DQ_SYSTEM -> new DQSystemReader(this);
			case EPD -> new EpdReader(this);
			case FLOW -> new FlowReader(this);
			case FLOW_PROPERTY -> new FlowPropertyReader(this);
			case IMPACT_CATEGORY -> new ImpactCategoryReader(this);
			case IMPACT_METHOD -> new ImpactMethodReader(this);
			case LOCATION -> new LocationReader(this);
			case PARAMETER -> new ParameterReader(this);
			case PROCESS -> new ProcessReader(this);
			case PRODUCT_SYSTEM -> new ProductSystemReader(this);
			case PROJECT -> new ProjectReader(this);
			case RESULT -> new ResultReader(this);
			case SOCIAL_INDICATOR -> new SocialIndicatorReader(this);
			case SOURCE -> new SourceReader(this);
			case UNIT_GROUP -> new UnitGroupReader(this);
			default -> null;
		};
	}

	private void copyBinaryFilesOf(ModelType type, String refId) {
		if (db == null || db.getFileStorageLocation() == null)
			return;
		var fs = new FileStore(db.getFileStorageLocation());
		try {
			var dir = fs.getFolder(type, refId);
			if (dir.exists()) {
				Dirs.clean(dir);
			}

			for (var path : reader.getBinFiles(type, refId)) {
				byte[] data = reader.getBytes(path);
				if (data == null)
					return;
				var name = Paths.get(path).getFileName().toString();
				if (!dir.exists()) {
					Files.createDirectories(dir.toPath());
				}
				File file = new File(dir, name);
				Files.write(file.toPath(), data);
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to import bin files for " + type + ":" + refId, e);
		}
	}
}
