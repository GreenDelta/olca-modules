package org.openlca.cloud.error;

import javax.ws.rs.core.Response.Status;

public class UnauthorizedRepositoryAccessException extends ServerException {

	private static final long serialVersionUID = -5922024730075076184L;

	public UnauthorizedRepositoryAccessException(String repositoryId) {
		super(Status.FORBIDDEN, "No access to '" + repositoryId + "'");
	}
}
