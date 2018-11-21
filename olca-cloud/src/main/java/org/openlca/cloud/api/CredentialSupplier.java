package org.openlca.cloud.api;

import java.util.function.Supplier;

public class CredentialSupplier {

	public final String username;
	final String password;
	// Method to call if token is required, if no callback is specified a
	// TokenRequiredException will be thrown when a token is required
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
