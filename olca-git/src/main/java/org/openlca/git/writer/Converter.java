package org.openlca.git.writer;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.model.ModelType;
import org.openlca.git.Config;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thavam.util.concurrent.blockingMap.BlockingHashMap;
import org.thavam.util.concurrent.blockingMap.BlockingMap;

/**
 * Multithread data conversion. Starts {config.converterThreads} simultaneous
 * threads to convert data sets to json/proto and afterwards starts the next
 * thread. To avoid memory issues when conversion is faster than consummation
 * "startNext" checks if the queueSize is reached and returns otherwise.
 * queueSize considers elements that are still in conversion as already part of
 * the queue. When elements are taken from the queue "startNext" is called again
 * to ensure continuation of threads
 * 
 * Expects all entries that are converted also to be taken in the same order,
 * otherwise runs into deadlock.
 * 
 * TODO check error handling
 */
class Converter {

	private static final Logger log = LoggerFactory.getLogger(Converter.class);
	private final Config config;
	private final BlockingMap<String, byte[]> queue = new BlockingHashMap<>();
	private final ExecutorService threads;
	private Deque<Diff> diffs;
	private final AtomicInteger queueSize = new AtomicInteger();

	Converter(Config config, ExecutorService threads) {
		this.config = config;
		this.threads = threads;
	}

	void start(List<Diff> diffs) {
		this.diffs = new LinkedList<>(diffs);
		for (var i = 0; i < config.converterThreads; i++) {
			startNext();
		}
	}

	private void startNext() {
		// forgoing synchronizing get + incrementAndGet for better performance.
		// might lead to temporarily slightly higher queueSize than specified
		if (queueSize.get() >= config.converterThreads)
			return;
		queueSize.incrementAndGet();
		synchronized (diffs) {
			if (diffs.isEmpty())
				return;
			var entry = diffs.pop();
			threads.submit(() -> {
				convert(entry);
				startNext();
			});
		}
	}

	private void convert(Diff diff) {
		if (diff.type == DiffType.DELETED)
			return;
		var path = diff.right.fullPath;
		var type = ModelType.valueOf(path.substring(0, path.indexOf('/'))).getModelClass();
		var name = path.substring(path.lastIndexOf('/') + 1);
		var refId = name.substring(0, name.indexOf('.'));
		try {
			var model = config.database.get(type, refId);
			var data = JsonWriter.convert(model, config);
			offer(path, data);
		} catch (Exception e) {
			log.error("failed to convert data set " + diff, e);
			offer(path, new byte[0]);
		}
	}

	private void offer(String refId, byte[] data) {
		try {
			queue.offer(refId, data);
		} catch (InterruptedException e) {
			log.error("failed to add element to data queue", e);
		}
	}

	byte[] take(String refId) throws InterruptedException {
		byte[] data = queue.take(refId);
		queueSize.decrementAndGet();
		startNext();
		return data;
	}

	void clear() {
		queue.clear();
	}

}
