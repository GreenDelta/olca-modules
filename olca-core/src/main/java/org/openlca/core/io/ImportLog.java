package org.openlca.core.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.openlca.core.model.descriptors.Descriptor;

/**
 * Contains log messages of a data import.
 */
public final class ImportLog {

	// using a set internally to avoid duplicate messages
	private final HashSet<Message> messages = new HashSet<>();
	private final List<Consumer<Message>> listeners = new ArrayList<>();

	public void listen(Consumer<Message> fn) {
		if (fn != null) {
			listeners.add(fn);
		}
	}

	public Collection<Message> messages() {
		return messages;
	}

	/**
	 * Info messages created via this method are not added to the log but are
	 * just passed to listeners that are attached to this log.
	 */
	public void info(String message) {
		if (listeners.isEmpty())
			return;
		var m = new Message(Type.INFO, message, null);
		for (var listener : listeners) {
			listener.accept(m);
		}
	}

	/**
	 * Creates an 'imported' message for the given descriptor.
	 */
	public void ok(Descriptor descriptor) {
		add(Type.OK, "imported", descriptor);
	}

	public void ok(String message) {
		add(Type.OK, message, null);
	}

	public void ok(String message, Descriptor descriptor) {
		add(Type.OK, message, descriptor);
	}

	public void warn(String message) {
		add(Type.WARNING, message, null);
	}

	public void warn(String message, Descriptor descriptor) {
		add(Type.WARNING, message, descriptor);
	}

	public void error(String message) {
		add(Type.ERROR, message, null);
	}

	public void error(String message, Descriptor descriptor) {
		add(Type.ERROR, message, descriptor);
	}

	public void error(String message, Throwable err) {
		add(Type.ERROR, message + ": " + err.getMessage(), null);
	}

	private void add(Type type, String message, Descriptor descriptor) {
		if (message == null && descriptor == null)
			return;
		var m = new Message(type, message, descriptor);
		if (messages.add(m)) {
			for (var listener : listeners) {
				listener.accept(m);
			}
		}
	}

	public enum Type {
		OK,
		WARNING,
		ERROR,
		INFO,
	}

	public record Message(
		Type type,
		String message,
		Descriptor descriptor) {

		public boolean hasMessage() {
			return message != null;
		}

		public boolean hasDescriptor() {
			return descriptor != null;
		}

		public boolean isOk() {
			return type == Type.OK;
		}

		public boolean isWarning() {
			return type == Type.WARNING;
		}

		public boolean isError() {
			return type == Type.ERROR;
		}

		public boolean isInfo() {
			return type == Type.INFO;
		}
	}
}
