package org.openlca.git.writer;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.git.model.Change;
import org.openlca.git.model.Change.ChangeType;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreWriter;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thavam.util.concurrent.blockingMap.BlockingHashMap;
import org.thavam.util.concurrent.blockingMap.BlockingMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Multithreaded data conversion. Starts {config.converterThreads} simultaneous
 * threads to convert data sets to JSON and afterwards starts the next thread.
 * To avoid memory issues when conversion is faster than consummation
 * "startNext" checks if the queueSize is reached and returns otherwise.
 * queueSize considers elements that are still in conversion as already part of
 * the queue. When elements are taken from the queue "startNext" is called again
 * to ensure continuation of threads
 * 
 * Expects all entries that are converted also to be taken in the same order,
 * otherwise runs into deadlock.
 * 
 * Product systems are not queued, but converted on demand, because the
 * JsonObjects during conversion take to much memory
 */
class Converter implements JsonStoreWriter {

	private static final Logger log = LoggerFactory.getLogger(Converter.class);
	private final BlockingMap<String, byte[]> queue = new BlockingHashMap<>();
	private final IDatabase database;
	private final ExecutorService threads;
	private final Deque<Change> changes = new LinkedList<>();
	private final Map<String, Change> systems = new HashMap<>();
	private final AtomicInteger queueSize = new AtomicInteger();
	private final JsonExport export;
	private final int converterThreads;
	private final UsedFeatures usedFeatures;
	private final ProgressMonitor progressMonitor;

	Converter(IDatabase database, ExecutorService threads, ProgressMonitor progressMonitor, UsedFeatures usedFeatures) {
		this.database = database;
		this.threads = threads;
		this.progressMonitor = progressMonitor;
		this.usedFeatures = usedFeatures;
		this.export = new JsonExport(database, this)
				.withReferences(false)
				.skipLibraryData(true)
				.skipExternalFiles(true);
		var processors = 1;
		try {
			processors = Runtime.getRuntime().availableProcessors();
		} catch (Throwable e) {
			processors = 1;
		}
		this.converterThreads = processors;
	}

	void start(List<Change> changes) {
		if (changes.isEmpty())
			return;
		this.changes.clear();
		this.systems.clear();
		for (var change : changes) {
			if (change.isCategory)
				continue;
			if (change.type == ModelType.PRODUCT_SYSTEM) {
				this.systems.put(change.path, change);
			} else {
				this.changes.add(change);
			}
		}
		for (var i = 0; i < converterThreads; i++) {
			startNext();
		}
	}

	private void startNext() {
		if (progressMonitor.isCanceled())
			return;
		// forgoing synchronizing get + incrementAndGet for better performance.
		// might lead to temporarily slightly higher queueSize than specified
		if (queueSize.get() >= converterThreads)
			return;
		queueSize.incrementAndGet();
		synchronized (changes) {
			if (changes.isEmpty())
				return;
			var entry = changes.pop();
			threads.submit(() -> {
				convert(entry);
				startNext();
			});
		}
	}

	private void convert(Change change) {
		if (change.changeType == ChangeType.DELETE)
			return;
		try {
			var model = database.get(change.type.getModelClass(), change.refId);
			convert(model);
		} catch (Exception e) {
			log.error("failed to convert data set " + change, e);
			put(change.path, new byte[0]);
		}
	}

	private void convert(RootEntity entity) {
		if (entity == null)
			return;
		try {
			export.write(entity);
		} catch (Exception e) {
			log.error("failed to serialize " + entity, e);
		}
	}

	@Override
	public void put(ModelType type, JsonObject object) {
		usedFeatures.checkSchemaVersion(object);
		var path = type.name() + "/";
		var category = Json.getString(object, "category");
		var refId = Json.getString(object, "@id");
		if (!Strings.nullOrEmpty(category)) {
			path += category + "/";
		}
		path += refId + GitUtil.DATASET_SUFFIX;
		put(path, object);
	}

	@Override
	public void put(String path, byte[] data) {
		try {
			queue.offer(path, data);
		} catch (InterruptedException e) {
			log.error("failed to add element to data queue", e);
		}
	}

	byte[] take(String path) throws InterruptedException {
		if (systems.containsKey(path))
			return convertProductSystem(systems.get(path));
		byte[] data = doTake(path);
		queueSize.decrementAndGet();
		startNext();
		return data;
	}

	private byte[] doTake(String path) throws InterruptedException {
		if (progressMonitor.isCanceled())
			return null;
		var data = queue.take(path, 1, TimeUnit.SECONDS);
		if (data != null)
			return data;
		return doTake(path);
	}

	private byte[] convertProductSystem(Change change) {
		if (progressMonitor.isCanceled())
			return null;
		var model = database.get(change.type.getModelClass(), change.refId);
		var object = export.getWriter(model).write(model);
		var json = new Gson().toJson(object);
		return json.getBytes(StandardCharsets.UTF_8);
	}

	void clear() {
		queue.clear();
	}

}
