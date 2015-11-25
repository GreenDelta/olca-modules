package org.openlca.cloud.model.data;

public class Commit {

	private String id;
	private String message;
	private String user;
	private long timestamp;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		String timestamp = Long.toString(this.timestamp);
		int length = id.length() + message.length() + user.length()
				+ timestamp.length() + 3; // +
											// 3
											// spaces
		StringBuilder string = new StringBuilder(length);
		string.append(id);
		string.append(' ');
		string.append(timestamp);
		string.append(' ');
		string.append(user);
		string.append(' ');
		string.append(message);
		return string.toString();
	}

	public static Commit parse(String value) {
		Commit entry = new Commit();
		int nextSpace = value.indexOf(' ');
		entry.setId(value.substring(0, nextSpace));
		value = value.substring(nextSpace + 1);
		nextSpace = value.indexOf(' ');
		entry.setTimestamp(Long.parseLong(value.substring(0, nextSpace)));
		value = value.substring(nextSpace + 1);
		nextSpace = value.indexOf(' ');
		entry.setUser(value.substring(0, nextSpace));
		entry.setMessage(value.substring(nextSpace + 1));
		return entry;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Commit))
			return false;
		return toString().equals(obj.toString());
	}

}
