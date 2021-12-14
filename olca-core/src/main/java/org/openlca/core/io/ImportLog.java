package org.openlca.core.io;

import java.util.Collection;
import java.util.HashSet;

import org.openlca.core.model.descriptors.Descriptor;

/**
 * Contains log messages of a data import.
 */
public final class ImportLog {

	// using a set internally to avoid duplicate messages
	private final HashSet<Message> messages = new HashSet<>();

	public Collection<Message> messages() {
		return messages;
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

	public void warning(String message) {
		add(Type.WARNING, message, null);
	}

	public void warning(String message, Descriptor descriptor) {
		add(Type.WARNING, message, descriptor);
	}

	public void error(String message) {
		add(Type.ERROR, message, null);
	}

	public void error(String message, Descriptor descriptor) {
		add(Type.ERROR, message, descriptor);
	}

	private void add(Type type, String message, Descriptor descriptor) {
		if (message == null && descriptor == null)
			return;
		messages.add(new Message(type, message, descriptor));
	}

	public enum Type {
		OK,
		WARNING,
		ERROR,
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
	}

}
