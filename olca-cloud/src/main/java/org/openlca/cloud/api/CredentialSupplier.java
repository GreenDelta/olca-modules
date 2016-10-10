package org.openlca.cloud.api;

import java.util.function.IntSupplier;

public class CredentialSupplier {

	public final String username;
	final String password;
	private IntSupplier tokenSupplier;

	public CredentialSupplier(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setTokenSupplier(IntSupplier tokenSupplier) {
		this.tokenSupplier = tokenSupplier;
	}

	int getToken() {
		return tokenSupplier.getAsInt();
	}

}
