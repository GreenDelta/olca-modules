package org.openlca.git.writer;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.git.model.Change;
import org.openlca.git.model.DiffType;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thavam.util.concurrent.blockingMap.BlockingHashMap;
import org.thavam.util.concurrent.blockingMap.BlockingMap;

/**
 * Multithread data conversion. Starts {config.converterThreads} simultaneous
 * threads to convert data sets to json and afterwards starts the next thread.
 * To avoid memory issues when conversion is faster than consummation
 * "startNext" checks if the queueSize is reached and returns otherwise.
 * queueSize considers elements that are still in conversion as already part of
 * the queue. When elements are taken from the queue "startNext" is called again
 * to ensure continuation of threads
 * 
 * Expects all entries that are converted also to be taken in the same order,
 * otherwise runs into deadlock.
 */
class Converter {

	private static final Logger log = LoggerFactory.getLogger(Converter.class);
	private static final int CONVERTER_THREADS = 50;
	private final BlockingMap<String, byte[]> queue = new BlockingHashMap<>();
	private final IDatabase database;
	private final ExecutorService threads;
	private Deque<Change> changes;
	private final AtomicInteger queueSize = new AtomicInteger();

	Converter(IDatabase database, ExecutorService threads) {
		this.database = database;
		this.threads = threads;
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
		var type = ModelType.valueOf(path.substring(0, path.indexOf('/'))).getModelClass();
		var name = path.substring(path.lastIndexOf('/') + 1);
		var refId = name.substring(0, name.indexOf('.'));
		try {
			var model = database.get(type, refId);
			var data = convert(model);
			offer(path, data);
		} catch (Exception e) {
			log.error("failed to convert data set " + change, e);
			offer(path, new byte[0]);
		}
	}

	private byte[] convert(RefEntity entity) {
		if (entity == null)
			return null;
		try {
			var json = JsonExport.toJson(entity, database);
			return json.toString().getBytes(StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("failed to serialize " + entity, e);
			return null;
		}
	}

	private void offer(String path, byte[] data) {
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
