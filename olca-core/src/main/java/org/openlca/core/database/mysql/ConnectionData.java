package org.openlca.core.database.mysql;

import javax.persistence.spi.PersistenceProvider;

import org.openlca.util.Strings;

/** Data for a MySQL connection. */
public final class ConnectionData {

	private String database;
	private String user;
	private String host = "localhost";
	private int port = 3306;
	private String password;
	private PersistenceProvider persistenceProvider;

	/**
	 * The persistence provider is required. Dependent on the JPA implementation
	 * and context (OSGi!) different persistent providers are possible.
	 */
	public void setPersistenceProvider(PersistenceProvider persistenceProvider) {
		this.persistenceProvider = persistenceProvider;
	}

	public PersistenceProvider getPersistenceProvider() {
		return persistenceProvider;
	}

	/** The database name is required for a connection. */
	public void setDatabase(String database) {
		this.database = database;
	}

	public String getDatabase() {
		return database;
	}

	/** The user name is required for a connection. */
	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	/** The host is optional (default value is 'localhost'). */
	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	/** The port is optional (default is 3306). */
	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	/** The password is optional (default is null). */
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return "jdbc:mysql://" + host + ":" + port + "/" + database;
	}

	@Override
	public String toString() {
		return getUrl();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (this == other)
			return true;
		if (!(other instanceof ConnectionData))
			return false;
		ConnectionData otherData = (ConnectionData) other;
		return this.getUrl().equals(otherData.getUrl())
				&& Strings.nullOrEqual(user, otherData.user)
				&& Strings.nullOrEqual(password, otherData.password);
	}

	@Override
	public int hashCode() {
		String s = this.getUrl() + user + password;
		return s.hashCode();
	}

}
