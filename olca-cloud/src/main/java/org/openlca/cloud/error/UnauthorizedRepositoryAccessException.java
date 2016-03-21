package org.openlca.cloud.error;

import javax.ws.rs.core.Response.Status;

public class UnauthorizedRepositoryAccessException extends ServerException {

	private static final long serialVersionUID = -5922024730075076184L;

	public UnauthorizedRepositoryAccessException(String repositoryId, String action) {
		super(Status.FORBIDDEN, "No permission to perform '" + action + "'" + " on '" + repositoryId + "'");
	}
}
