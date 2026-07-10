package org.openlca.io.olca.migration.results;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openlca.io.olca.TransferContext;

class TransferTask implements Runnable {

	private final TransferContext ctx;
	private final BlockingQueue<QueueItem> queue;
	private volatile String error;

	TransferTask(TransferContext ctx) {
		this.ctx = ctx;
		this.queue = new ArrayBlockingQueue<>(100);
	}

	void put(QueueItem item) {
		try {
			queue.put(item);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			error = "Failed to add Result to transfer queue: " + e.getMessage();
		}
	}

	void stop() {
		try {
			queue.put(QueueItem.stop());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	boolean hasError() {
		return error != null;
	}

	String error() {
		return error;
	}

	@Override
	public void run() {
		try {
			while (true) {
				if (error != null)
					break;
				var item = queue.take();
				if (item.isStop())
					break;
				ctx.resolve(item.result());

			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			error = "Database transfer failed: " + e.getMessage();
		}
	}
}
