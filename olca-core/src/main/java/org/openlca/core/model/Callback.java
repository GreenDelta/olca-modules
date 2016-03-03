package org.openlca.core.model;

/**
 * A simple callback interface for notification of model events; e.g. for
 * imports and exports.
 */
@FunctionalInterface
public interface Callback {

	/**
	 * Executes the callback function. This function follows the error-first
	 * pattern as known from Node.js but with a message object except of plain
	 * error.
	 */
	void apply(Message message, RootEntity data);

	class Message {

		public static final int INFO = 0;
		public static final int WARN = 1;
		public static final int ERROR = 2;

		public final String text;
		public final int type;
		public final Throwable error;

		private Message(int type, String text, Throwable error) {
			this.type = type;
			this.text = text;
			this.error = error;
		}

		public static Message info(String text) {
			return new Message(INFO, text, null);
		}

		public static Message warn(String text) {
			return new Message(WARN, text, null);
		}

		public static Message warn(String text, Throwable error) {
			return new Message(WARN, text, error);
		}

		public static Message error(String text) {
			return new Message(ERROR, text, null);
		}

		public static Message error(String text, Throwable error) {
			return new Message(ERROR, text, error);
		}
	}
}
