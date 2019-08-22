package org.openlca.io.maps;

import java.util.Objects;

/**
 * Status describes the state a mapping entry or flow reference can have when
 * when synchronized or applied on a database.
 */
public class Status {

	public static final int OK = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;

	public final int type;
	public final String message;

	public Status(int type, String message) {
		this.type = type;
		this.message = message;
	}

	public static Status ok() {
		return ok("ok");
	}

	public static Status ok(String message) {
		return new Status(OK, message);
	}

	public static Status warn(String message) {
		return new Status(WARNING, message);
	}

	public static Status error(String message) {
		return new Status(ERROR, message);
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
	public Status clone() {
		return new Status(type, message);
	}

	@Override
	public String toString() {
		String s = "";
		switch (type) {
		case OK:
			s += "ok: ";
			break;
		case WARNING:
			s += "warning: ";
			break;
		case ERROR:
			s += "error: ";
			break;
		default:
			break;
		}
		return s + message;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Status))
			return false;
		Status other = (Status) obj;
		if (this.type != other.type)
			return false;
		return Objects.equals(this.message, other.message);
	}

	/**
	 * Returns the status from the given string. Note that the status is null
	 * when the given string is null or empty.
	 */
	static Status fromString(String s) {
		if (s == null)
			return null;
		String t = s.trim();
		if (t.length() == 0)
			return null;
		int type = OK;
		if (t.startsWith("ok:")) {
			t = t.substring(3).trim();
		} else if (t.startsWith("warning:")) {
			type = WARNING;
			t = t.substring(8).trim();
		} else if (t.startsWith("error:")) {
			type = ERROR;
			t = t.substring(6).trim();
		}
		return new Status(type, t);
	}
}
