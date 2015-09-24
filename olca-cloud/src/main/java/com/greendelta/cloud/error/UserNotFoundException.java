package com.greendelta.cloud.error;

import javax.ws.rs.core.Response.Status;

public class UserNotFoundException extends ServerException {

	private static final long serialVersionUID = 3597855854783144681L;

	public UserNotFoundException(String username) {
		super(Status.NOT_FOUND, "No user '" + username + "' found");
	}

}
