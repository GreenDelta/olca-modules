package org.openlca.core.database.mysql;

/**
 * The descriptor of a database.
 */
public class DatabaseDescriptor {

	private String name;
	private boolean upToDate;
	private int version;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isUpToDate() {
		return upToDate;
	}

	public void setUpToDate(boolean upToDate) {
		this.upToDate = upToDate;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}

}
