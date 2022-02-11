package org.openlca.core.io;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.model.CategorizedEntity;
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

	public ImportLog() {
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

	public int countOf(State state) {
		if (state == null)
			return 0;
		var count = new Object() {
			int value = 0;
		};
		if (state == State.ERROR) {
			count.value += errors.size();
		} else if (state == State.WARNING) {
			count.value += warnings.size();
		}
		eachInLog(message -> {
			if (message.state == state) {
				count.value++;
			}
		});
		return count.value;
	}

	public Set<Message> messages() {
		var all = new HashSet<>(errors);
		all.addAll(warnings);
		eachInLog(all::add);
		return all;
	}

	public Set<Message> messagesOf(State state, State... more) {
		Predicate<State> matches = s -> {
			if (s == state)
				return true;
			if (more == null)
				return false;
			for (var si : more) {
				if (si == s)
					return true;
			}
			return false;
		};

		var matched = new HashSet<Message>();
		if (matches.test(State.ERROR)) {
			matched.addAll(errors);
		}
		if (matches.test(State.WARNING)) {
			matched.addAll(warnings);
		}
		eachInLog(message -> {
			if (matches.test(message.state)) {
				matched.add(message);
			}
		});
		return matched;
	}

	private void eachInLog(Consumer<Message> fn) {
		var it = logs.iterator();
		while (it.hasNext()) {
			it.advance();
			var message = it.value();
			fn.accept(message);
		}
	}

	public void updated(CategorizedEntity entity) {
		add(State.UPDATED, entity);
	}

	public void imported(CategorizedEntity entity) {
		add(State.IMPORTED, entity);
	}

	public void skipped(CategorizedEntity entity) {
		add(State.SKIPPED, entity);
	}

	private void add(State state, CategorizedEntity e) {
		if (e == null || e.id == 0)
			return;
		var current = logs.get(e.id);
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

	public void error(String message) {
		if (message == null)
			return;
		add(new Message(State.ERROR, message, null));
	}

	public void error(String message, Throwable err) {
		error(message + ": " + err.getMessage());
	}

	private void add(Message message) {
		if (message.descriptor != null && logs.size() < MAX_SIZE) {
			logs.put(message.descriptor.id, message);
		} else if (message.isError() && errors.size() < MAX_SIZE) {
			errors.add(message);
		} else if (message.isWarning() && warnings.size() < MAX_SIZE) {
			warnings.add(message);
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
