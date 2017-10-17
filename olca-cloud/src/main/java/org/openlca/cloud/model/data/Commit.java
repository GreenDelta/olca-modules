package org.openlca.cloud.model.data;

public class Commit {

	public String id;
	public String message;
	public String user;
	public long timestamp;

	@Override
	public String toString() {
		String timestamp = Long.toString(this.timestamp);
		int length = id.length() + message.length() + user.length() + timestamp.length() + 3;
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
		entry.id = value.substring(0, nextSpace);
		value = value.substring(nextSpace + 1);
		nextSpace = value.indexOf(' ');
		entry.timestamp = Long.parseLong(value.substring(0, nextSpace));
		value = value.substring(nextSpace + 1);
		nextSpace = value.indexOf(' ');
		entry.user = value.substring(0, nextSpace);
		entry.message = value.substring(nextSpace + 1);
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
