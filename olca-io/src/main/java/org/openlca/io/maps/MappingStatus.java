package org.openlca.io.maps;

import org.openlca.core.model.Copyable;
import org.openlca.util.Strings;

/**
 * Status describes the state a mapping entry or flow reference can have when
 * synchronized or applied on a database.
 */
public record MappingStatus(int type, String message)
	implements Copyable<MappingStatus> {

	public static final int OK = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	public static final int EMPTY = 4;
	private static final MappingStatus empty = new MappingStatus(EMPTY, "");

	public static MappingStatus empty() {
		return empty;
	}

	public static MappingStatus ok() {
		return ok("ok");
	}

	public static MappingStatus ok(String message) {
		return new MappingStatus(OK, message);
	}

	public static MappingStatus warn(String message) {
		return new MappingStatus(WARNING, message);
	}

	public static MappingStatus error(String message) {
		return new MappingStatus(ERROR, message);
	}

	public boolean isEmpty() {
		return type == EMPTY;
	}

	public boolean isOk() {
		return type == OK;
	}

	public boolean isWarning() {
		return type == WARNING;
	}

	public boolean isError() {
		return type == ERROR;
	}

	@Override
	public MappingStatus copy() {
		return new MappingStatus(type, message);
	}

	@Override
	public String toString() {
		var prefix = switch (type) {
			case OK -> "ok: ";
			case WARNING -> "warning: ";
			case ERROR -> "error: ";
			default -> "";
		};
		return prefix + message;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof MappingStatus other))
			return false;
		if (this.type != other.type)
			return false;
		return Strings.nullOrEqual(this.message, other.message);
	}

	/**
	 * Returns the status from the given string.
	 */
	public static MappingStatus fromString(String s) {
		if (s == null)
			return empty;
		String message = s.trim();
		if (message.length() == 0)
			return empty;
		int type = OK;
		if (message.startsWith("ok:")) {
			message = message.substring(3).trim();
		} else if (message.startsWith("warning:")) {
			type = WARNING;
			message = message.substring(8).trim();
		} else if (message.startsWith("error:")) {
			type = ERROR;
			message = message.substring(6).trim();
		}
		return new MappingStatus(type, message);
	}
}
