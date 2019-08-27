package org.openlca.core.database.postgres;

public class PostgresConnParams implements Cloneable{
	private String host;
	private String port;
	private String dbName;
	private String user;
	private String password;
	
	public PostgresConnParams() {
	}

	public PostgresConnParams(String host, String port, String dbName, String user, String password) {
      this.host = host;
      this.port = port;
      this.dbName = dbName;
      this.user = user;
      this.password = password;
    }

    public String getHost() {
		return host;
	}

	public PostgresConnParams setHost(String host) {
		this.host = host;
		return this;
	}

	public String getPort() {
		return port;
	}

	public PostgresConnParams setPort(String port) {
		this.port = port;
		return this;
	}

	public String getDbName() {
		return dbName;
	}

	public PostgresConnParams setDbName(String dbName) {
		this.dbName = dbName;
		return this;
	}

	public String getUser() {
		return user;
	}

	public PostgresConnParams setUser(String user) {
		this.user = user;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public PostgresConnParams setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getJdbcUrl() {
		return String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
	}

	public PostgresConnParams clone() throws CloneNotSupportedException {
		return (PostgresConnParams) super.clone();
	}
}
