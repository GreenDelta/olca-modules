package org.openlca.cloud.error;

import javax.ws.rs.core.Response.Status;

public class InvalidRepositoryNameException extends ServerException {

	private static final long serialVersionUID = 3597855854783144689L;

	public InvalidRepositoryNameException(String repositoryId) {
		super(Status.BAD_REQUEST, "Invalid repository name '" + repositoryId + "'");
	}

}
