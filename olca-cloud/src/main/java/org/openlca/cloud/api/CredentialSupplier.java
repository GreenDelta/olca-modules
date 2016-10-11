package org.openlca.cloud.api;

import java.util.function.Supplier;

public class CredentialSupplier {

	public final String username;
	final String password;
	private Supplier<Integer> tokenSupplier;

	public CredentialSupplier(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setTokenSupplier(Supplier<Integer> tokenSupplier) {
		this.tokenSupplier = tokenSupplier;
	}

	Integer getToken() {
		return tokenSupplier.get();
	}

}
