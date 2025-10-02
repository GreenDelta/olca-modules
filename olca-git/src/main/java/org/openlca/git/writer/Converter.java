package org.openlca.git.writer;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Callback;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreWriter;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

class Converter implements JsonStoreWriter {

	private static final Logger log = LoggerFactory.getLogger(Converter.class);
	private final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(1);
	private final Deque<Diff> changes = new LinkedList<>();
	private final IDatabase database;
	private final JsonExport export;
	private final UsedFeatures usedFeatures;
	private final ProgressMonitor progressMonitor;
	private boolean closed = false;

	Converter(IDatabase database, ProgressMonitor progressMonitor, UsedFeatures usedFeatures, List<Diff> changes) {
		this.database = database;
		this.progressMonitor = progressMonitor;
		this.usedFeatures = usedFeatures;
		this.changes.addAll(changes.stream()
				.filter(this::needsConversion)
				.sorted()
				.toList());
		this.export = new JsonExport(database, this)
				.withReferences(false)
				.skipLibraryData(true)
				.skipExternalFiles(true);
	}

	private boolean needsConversion(Diff change) {
		return change.diffType != DiffType.DELETED
				&& !change.isRepositoryInfo
				&& !change.isLibrary
				&& !change.isCategory
				&& change.type != null
				&& change.refId != null;
	}

	void start() {
		Thread.startVirtualThread(() -> {
			while (!changes.isEmpty() && !progressMonitor.isCanceled() && !closed) {
				var entry = changes.pop();
				convert(entry);
			}
		});
	}

	private void convert(Diff change) {
		try {
			var model = database.get(change.type.getModelClass(), change.refId);
			export.write(model, (message, data) -> {
				if (message.type == Callback.Message.ERROR) {
					handleError(change, message.error);
				}
			});
		} catch (Exception e) {
			handleError(change, e);
		}
	}

	private void handleError(Diff change, Throwable e) {
		log.error("failed to convert data set " + change, e);
		put(change.path, (byte[]) null);		
	}
	
	@Override
	public void put(ModelType type, JsonObject object) {
		usedFeatures.checkSchemaVersion(object);
		var category = Json.getString(object, "category");
		var refId = Json.getString(object, "@id");
		var path = GitUtil.toDatasetPath(type, category, refId);
		put(path, object);
	}

	@Override
	public void put(String path, byte[] data) {
		try {
			queue.put(data);
		} catch (InterruptedException e) {
			log.error("failed to add element to data queue", e);
		}
	}

	byte[] take(String path) throws InterruptedException {
		if (progressMonitor.isCanceled() || closed)
			return null;
		return queue.take();
	}

	void close() {
		// if the converter is still trying to put an element it will be able to
		// after queue.clear() is called. Since we set closed=true before, no
		// conversion will be started afterwards, making sure the thread ends
		closed = true;
		queue.clear();
	}

}
