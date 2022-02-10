package org.openlca.core.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;

/**
 * Contains log messages of a data import.
 */
public final class ImportLog {

	private final int MAX_SIZE;

	private final TLongObjectHashMap<Descriptor> logs = new TLongObjectHashMap<>();

	// using a set internally to avoid duplicate messages
	private final HashSet<Message> messages = new HashSet<>();
	private final List<Consumer<Message>> listeners = new ArrayList<>();

	public ImportLog () {
		this(10_000);
	}

	private ImportLog(int size) {
		MAX_SIZE = size;
	}

	public static ImportLog ofSize(int size) {
		return new ImportLog(size);
	}

	public void listen(Consumer<Message> fn) {
		if (fn != null) {
			listeners.add(fn);
		}
	}

	public void updated(CategorizedEntity entity) {
		updated(Descriptor.of(entity));
	}

	public void updated(CategorizedDescriptor d) {
		if (d == null || d.id == 0)
			return;

	}

	/**
	 * Info messages created via this method are not added to the log but are
	 * just passed to listeners that are attached to this log.
	 */
	public void info(String message) {
		if (listeners.isEmpty())
			return;
		var m = new Message(State.INFO, message, null);
		for (var listener : listeners) {
			listener.accept(m);
		}
	}


	/**
	 * Creates an 'imported' message for the given descriptor.
	 */
	public void ok(Descriptor descriptor) {
		add(State.OK, "imported", descriptor);
	}

	public void ok(String message) {
		add(State.OK, message, null);
	}

	public void ok(String message, Descriptor descriptor) {
		add(State.OK, message, descriptor);
	}

	public void warn(String message) {
		add(State.WARNING, message, null);
	}

	public void warn(String message, Descriptor descriptor) {
		add(State.WARNING, message, descriptor);
	}

	public void error(String message) {
		add(State.ERROR, message, null);
	}

	public void error(String message, Descriptor descriptor) {
		add(State.ERROR, message, descriptor);
	}

	public void error(String message, Throwable err) {
		add(State.ERROR, message + ": " + err.getMessage(), null);
	}

	private void add(State type, String message, Descriptor descriptor) {
		if (message == null && descriptor == null)
			return;
		var m = new Message(type, message, descriptor);
		if (messages.add(m)) {
			for (var listener : listeners) {
				listener.accept(m);
			}
		}
	}

	public enum State {
		IMPORTED,
		UPDATED,
		SKIPPED,
		ERROR,
		WARNING,
		INFO,
	}

	public record Message(
		State state,
		String message,
		Descriptor descriptor) {

		public boolean hasMessage() {
			return message != null;
		}

		public boolean hasDescriptor() {
			return descriptor != null;
		}

		public boolean isOk() {
			if (state == null)
				return true;
			return switch (state) {
				case ERROR, WARNING -> false;
				default -> true;
			};
		}

		public boolean isWarning() {
			return state == State.WARNING;
		}

		public boolean isError() {
			return state == State.ERROR;
		}

		private int priority() {
			if (state == null)
				return 0;
			return switch (state) {
				case INFO -> 1;
				case SKIPPED -> 2;
				case UPDATED -> 3;
				case IMPORTED -> 4;
				case WARNING -> 5;
				case ERROR -> 6;
			};
		}

	}
}
