package org.openlca.core.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

	private final TLongObjectHashMap<Message> logs = new TLongObjectHashMap<>();
	private final HashSet<Message> errors = new HashSet<>();
	private final HashSet<Message> warnings = new HashSet<>();
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
		add(State.UPDATED, entity);
	}

	public void imported(CategorizedEntity entity) {
		add(State.IMPORTED, entity);
	}

	private void add(State state, CategorizedEntity e) {
		if (e == null || e.id == 0)
			return;
		var current = logs.get(e.id);
		if (current != null && current.priority() >= state.priority())
			return;
		var message = new Message(state, null, Descriptor.of(e));
		add(message);
	}

	/**
	 * Info messages created via this method are not added to the log but are
	 * just passed to listeners that are attached to this log.
	 */
	public void info(String message) {
		if (message == null)
			return;
		add(new Message(State.INFO, message, null));
		if (listeners.isEmpty())
			return;
		var m = new Message(State.INFO, message, null);
		for (var listener : listeners) {
			listener.accept(m);
		}
	}

	public void warn(String message) {
		add(new Message(State.WARNING, message, null));
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

	private void add(Message message) {
		if (message.descriptor != null && logs.size() < MAX_SIZE) {
			logs.put(message.descriptor.id, message);
		}
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
		INFO;

		private int priority() {
			return switch (this) {
				case INFO -> 1;
				case SKIPPED -> 2;
				case UPDATED -> 3;
				case IMPORTED -> 4;
				case WARNING -> 5;
				case ERROR -> 6;
			};
		}
	}

	public record Message(
		State state,
		String message,
		CategorizedDescriptor descriptor) {

		private Message(State state, String message) {
			this(state, message, null);
		}

		private Message(State state, CategorizedDescriptor descriptor) {
			this(state, null, descriptor);
		}

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
			return state != null
				? state.priority()
				: 0;
		}

		@Override
		public int hashCode() {
			return 31 * (31 * Objects.hashCode(state)
				* Objects.hashCode(message))
				* Objects.hashCode(descriptor);
		}
	}
}
