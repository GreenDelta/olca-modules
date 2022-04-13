package org.openlca.core.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.slf4j.LoggerFactory;

/**
 * Contains log messages of a data import.
 */
public final class ImportLog {

	private final int MAX_SIZE;

	// We only keep the most important message per data set (i.e. to not have
	// multiple update messages for the same data set) and index the data set
	// messages by the database internal ID of the imported data set.
	private final TLongObjectHashMap<Message> dataSetLogs = new TLongObjectHashMap<>();
	private final HashSet<Message> otherLogs = new HashSet<>();

	private final List<Consumer<Message>> listeners = new ArrayList<>();

	public ImportLog() {
		this(100_000);
	}

	private ImportLog(int size) {
		MAX_SIZE = size;
	}

	/**
	 * Creates an import log with of the given size.
	 *
	 * @param size the maximum number of messages that the log can store.
	 */
	public static ImportLog ofSize(int size) {
		return new ImportLog(size);
	}

	public void listen(Consumer<Message> fn) {
		if (fn != null) {
			listeners.add(fn);
		}
	}

	public int size() {
		return dataSetLogs.size() + otherLogs.size();
	}

	public int countOf(State state) {
		if (state == null)
			return 0;
		var count = new Object() {
			int value = 0;
		};
		Consumer<Message> filter = message -> {
			if (message.state == state) {
				count.value++;
			}
		};
		eachWithDataSet(filter);
		otherLogs.forEach(filter);
		return count.value;
	}

	public Collection<Message> messages() {
		var all = new ArrayList<Message>(size());
		all.addAll(otherLogs);
		all.addAll(dataSetLogs.valueCollection());
		return all;
	}

	public Set<Message> messagesOf(State state, State... more) {
		var matched = new HashSet<Message>();
		Consumer<Message> filter = message -> {
			if (state == message.state) {
				matched.add(message);
				return;
			}
			if (more == null)
				return;
			for (var si : more) {
				if (si == message.state) {
					matched.add(message);
					return;
				}
			}
		};
		eachWithDataSet(filter);
		otherLogs.forEach(filter);
		return matched;
	}

	private void eachWithDataSet(Consumer<Message> fn) {
		var it = dataSetLogs.iterator();
		while (it.hasNext()) {
			it.advance();
			var message = it.value();
			fn.accept(message);
		}
	}

	public void updated(RootEntity entity) {
		add(State.UPDATED, entity);
	}

	public void imported(RootEntity entity) {
		add(State.IMPORTED, entity);
	}

	public void skipped(RootEntity entity) {
		add(State.SKIPPED, entity);
	}

	private void add(State state, RootEntity e) {
		if (e == null || e.id == 0)
			return;
		var current = dataSetLogs.get(e.id);
		if (current != null && current.priority() >= state.priority())
			return;
		add(new Message(state, Descriptor.of(e)));
	}

	/**
	 * Info messages created via this method are not added to the log but are
	 * just passed to listeners that are attached to this log.
	 */
	public void info(String message) {
		if (message == null)
			return;
		add(new Message(State.INFO, message));
	}

	public void warn(String message) {
		if (message == null)
			return;
		add(new Message(State.WARNING, message));
	}

	public void warn( RootEntity e, String message) {
		if (e == null && message == null)
			return;
		add(new Message(State.WARNING, message, Descriptor.of(e)));
	}

	public void error(String message) {
		if (message == null)
			return;
		add(new Message(State.ERROR, message, null));
	}

	public void error(String message, Throwable err) {
		error(message + ": " + err.getMessage());
	}

	private void add(Message message) {
		if (size() >= MAX_SIZE)
			return;
		if (message.hasDescriptor()) {
			dataSetLogs.put(message.descriptor.id, message);
		} else {
			otherLogs.add(message);
		}
		for (var listener : listeners) {
			listener.accept(message);
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
		RootDescriptor descriptor) {

		private Message(State state, String message) {
			this(state, message, null);
		}

		private Message(State state, RootDescriptor descriptor) {
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

		public void log() {
			if (state == null || (message == null && descriptor == null))
				return;
			var log = LoggerFactory.getLogger(Message.class);
			switch (state) {
				case ERROR -> {
					if (descriptor == null) {
						log.error(message);
					} else if (message == null) {
						log.error("import failed for {}", descriptor);
					} else {
						log.error("{}: {}", message, descriptor);
					}
				}
				case WARNING -> {
					if (descriptor == null) {
						log.warn(message);
					} else if (message == null) {
						log.warn("import warning for {}", descriptor);
					} else {
						log.warn("{}: {}", message, descriptor);
					}
				}
				case INFO -> {
					if (descriptor == null) {
						log.info(message);
					} else if (message == null) {
						log.info("import info for {}", descriptor);
					} else {
						log.info("{}: {}", message, descriptor);
					}
				}
				default -> {
					if (descriptor == null) {
						log.info("{}, {}", message, state);
					} else if (message == null) {
						log.info("{}, {}", descriptor, state);
					} else {
						log.info("{}: {}, {}", message, descriptor, state);
					}
				}
			}
		}

		@Override
		public int hashCode() {
			return 31 * (31 * Objects.hashCode(state)
				* Objects.hashCode(message))
				* Objects.hashCode(descriptor);
		}
	}
}
