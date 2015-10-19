package org.openlca.cloud.error;

import javax.ws.rs.core.Response.Status;

public class RepositoryNotFoundException extends ServerException {

	private static final long serialVersionUID = 3597855854783144681L;

	public RepositoryNotFoundException(String repositoryId) {

		super(Status.NOT_FOUND, "No repository '" + repositoryId + "' found");
	}

}
