package org.openlca.core.math;

/**
 * Converted script exception
 */
public class FormulaParseException extends Exception {

	private static final long serialVersionUID = 7247274296250802196L;

	private final String message;
	private String triggerId;

	public FormulaParseException(Exception e) {
		message = e.getMessage();
	}

	public FormulaParseException(String message) {
		this.message = message;
	}

	protected void setTriggerId(final String triggerId) {
		this.triggerId = triggerId;
	}

	@Override
	public String getMessage() {
		return mapMessage();
	}

	private String mapMessage() {
		String original = message;
		String mappedMessage = null;
		try {
			if (message.contains("ReferenceError")) {
				original = original.substring(original.indexOf("\"") + 1);
				String param = original.substring(0, original.indexOf("\""));
				mappedMessage = "Parameter " + param + " is not defined";
			} else {
				mappedMessage = message;
			}
		} catch (Exception e) {
			mappedMessage = message;
		}
		return mappedMessage;
	}

	public String getTriggerId() {
		return triggerId;
	}

}
