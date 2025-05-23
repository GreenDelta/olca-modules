package org.openlca.jsonld.input;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.CategorySync;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
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
	final Map<Class<?>, ModelType> types = new HashMap<>();

	private final ImportCache cache = new ImportCache(this);
	private final ExchangeProviderQueue providers;

	public JsonImport(JsonStoreReader reader, IDatabase db) {
		this.db = db;
		this.reader = Upgrades.chain(reader);
		this.providers = ExchangeProviderQueue.create(db);
		this.categories = CategorySync.of(db);
		for (var type : ModelType.values()) {
			types.put(type.getModelClass(), type);
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

	void visited(RootEntity entity) {
		if (entity instanceof Process p) {
			providers.pop(p);
		}
		cache.visited(entity);
	}

	public ExchangeProviderQueue providers() {
		return providers;
	}

	void imported(RefEntity entity) {
		if (callback == null)
			return;
		callback.accept(entity);
	}

	public void run(ModelType type, String id) {
		if (type == null || id == null)
			return;
		get(type.getModelClass(), id);
	}

	@Override
	public void run() {
		new CategoryImport(this).importAll();
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
				for (var id : reader.getRefIds(type)) {
					run(type, id);
				}
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

		var item = cache.fetch(type, refId);
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

		copyBinaryFilesOf(modelType, refId);
		visited(model);
		imported(model);

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
		if (providerId == null || exchange == null)
			return;
		var type = ProviderType.toModelClass(exchange.defaultProviderType);
		if (type == Process.class) {
			providers.add(providerId, exchange);
			return;
		}
		var d = getDescriptor(type, providerId);
		if (d != null) {
			exchange.defaultProviderId = d.id;
		}
	}

	EntityReader<?> readerFor(ModelType type) {
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

	void copyBinaryFilesOf(ModelType type, String refId) {
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
					continue;
				var name = fileNameOf(path);
				if (name == null)
					continue;
				if (!dir.exists()) {
					Files.createDirectories(dir.toPath());
				}
				File file = new File(dir, name);
				Files.write(file.toPath(), data);
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to import bin files for {}:{}", type, refId, e);
		}
	}

	private String fileNameOf(String path) {
		if (path == null)
			return null;
		int lastSep = -1;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (c == '/' || c == '\\') {
				lastSep = i;
			}
		}
		if (lastSep == -1)
			return path; // no separator
		if (lastSep == (path.length() -1))
			return null; // no file name, should never happen
		return path.substring(lastSep + 1);
	}
}
