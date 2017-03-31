package org.openlca.cloud.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ServerException extends WebApplicationException {

	private static final long serialVersionUID = 3597855854783144682L;

	private Status status;
	private String message;

	public ServerException(Status status, String message) {
		this.message = message;
		this.status = status;
	}

	@Override
	public Response getResponse() {
		return Response.status(status).entity(message).build();
	}

}
