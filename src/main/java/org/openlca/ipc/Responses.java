package org.openlca.ipc;

class Responses {

	static RcpResponse serverError(Exception e) {
		RcpError error = new RcpError();
		error.code = -32000;
		error.message = "An internal server error occurred: " + e.getMessage();
		RcpResponse response = new RcpResponse();
		response.error = error;
		return response;
	}
}
