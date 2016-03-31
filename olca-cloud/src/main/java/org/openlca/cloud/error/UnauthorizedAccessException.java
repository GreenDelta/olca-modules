package org.openlca.cloud.error;

import javax.ws.rs.core.Response.Status;

import com.google.common.base.Strings;

public class UnauthorizedAccessException extends ServerException {

	private static final long serialVersionUID = -5922024730075076184L;

	public UnauthorizedAccessException(String path, String action) {
		super(Status.FORBIDDEN, "No permission to perform '" + action + "'"
				+ (Strings.isNullOrEmpty(path) ? "" : " on '" + path + "'"));
	}
}
