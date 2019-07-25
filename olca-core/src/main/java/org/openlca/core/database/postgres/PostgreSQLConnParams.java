package org.openlca.core.database.postgres;

public class PostgreSQLConnParams implements Cloneable{
	private String host;
	private String port;
	private String dbName;
	private String user;
	private String password;

	public String getHost() {
		return host;
	}

	public PostgreSQLConnParams setHost(String host) {
		this.host = host;
		return this;
	}

	public String getPort() {
		return port;
	}

	public PostgreSQLConnParams setPort(String port) {
		this.port = port;
		return this;
	}

	public String getDbName() {
		return dbName;
	}

	public PostgreSQLConnParams setDbName(String dbName) {
		this.dbName = dbName;
		return this;
	}

	public String getUser() {
		return user;
	}

	public PostgreSQLConnParams setUser(String user) {
		this.user = user;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public PostgreSQLConnParams setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getJdbcUrl() {
		return String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
	}

	public PostgreSQLConnParams clone() throws CloneNotSupportedException {
		return (PostgreSQLConnParams) super.clone();
	}
}
