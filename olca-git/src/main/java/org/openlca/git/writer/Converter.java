package org.openlca.git.writer;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.git.model.Change;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.GitUtil;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreWriter;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thavam.util.concurrent.blockingMap.BlockingHashMap;
import org.thavam.util.concurrent.blockingMap.BlockingMap;

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
 */
class Converter implements JsonStoreWriter {

	private static final Logger log = LoggerFactory.getLogger(Converter.class);
	private static final int CONVERTER_THREADS = 50;
	private final BlockingMap<String, byte[]> queue = new BlockingHashMap<>();
	private final IDatabase database;
	private final ExecutorService threads;
	private Deque<Change> changes;
	private final AtomicInteger queueSize = new AtomicInteger();
	private final JsonExport export;

	Converter(IDatabase database, ExecutorService threads) {
		this.database = database;
		this.threads = threads;
		this.export = new JsonExport(database, this)
				.withReferences(false)
				.skipExternalFiles(true);
	}

	void start(List<Change> changes) {
		this.changes = new LinkedList<>(changes);
		for (var i = 0; i < CONVERTER_THREADS; i++) {
			startNext();
		}
	}

	private void startNext() {
		// forgoing synchronizing get + incrementAndGet for better performance.
		// might lead to temporarily slightly higher queueSize than specified
		if (queueSize.get() >= CONVERTER_THREADS)
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
		if (change.diffType == DiffType.DELETED)
			return;
		var path = change.path;
		var type = (Class<? extends RootEntity>) ModelType
				.valueOf(path.substring(0, path.indexOf('/')))
				.getModelClass();
		var name = path.substring(path.lastIndexOf('/') + 1);
		var refId = name.substring(0, name.indexOf('.'));
		try {
			var model = database.get(type, refId);
			convert(model);
		} catch (Exception e) {
			log.error("failed to convert data set " + change, e);
			put(path, new byte[0]);
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
		byte[] data = queue.take(path);
		queueSize.decrementAndGet();
		startNext();
		return data;
	}

	void clear() {
		queue.clear();
	}

}
