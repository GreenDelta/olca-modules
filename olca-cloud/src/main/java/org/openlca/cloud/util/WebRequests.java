package org.openlca.cloud.util;

import java.io.InputStream;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class WebRequests {

	private static final Logger log = LoggerFactory.getLogger(WebRequests.class);
	private static final Client client;

	static {
		client = Client.create();
		client.setChunkedEncodingSize(1024 * 100); // 100kb
	}

	public static ClientResponse call(Type type, String url, String sessionId) throws WebRequestException {
		return call(type, url, sessionId, null);
	}

	public static ClientResponse call(Type type, String url, String sessionId, Object data) throws WebRequestException {
		log.info(type.name() + " " + url);
		Builder request = builder(url, sessionId, data);
		ClientResponse response = call(type, request);
		if (response.getStatus() >= 400 && response.getStatus() <= 599)
			throw new WebRequestException(response);
		return response;
	}

	private static ClientResponse call(Type type, Builder builder) {
		switch (type) {
		case GET:
			return builder.get(ClientResponse.class);
		case POST:
			return builder.post(ClientResponse.class);
		case PUT:
			return builder.put(ClientResponse.class);
		case DELETE:
			return builder.delete(ClientResponse.class);
		default:
			return null;
		}
	}

	private static Builder builder(String url, String sessionId, Object data) {
		WebResource resource = client.resource(url);
		Builder builder = resource.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE);
		if (sessionId != null)
			builder.cookie(new Cookie("JSESSIONID", sessionId));
		if (data == null)
			return builder;
		if (data instanceof InputStream)
			builder.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		else
			builder.entity(new Gson().toJson(data), MediaType.APPLICATION_JSON_TYPE);
		return builder;
	}

	public static enum Type {
		GET, POST, PUT, DELETE;
	}

	public static class WebRequestException extends Exception {

		private static final long serialVersionUID = 1423557937866180113L;
		private int errorCode;

		private WebRequestException(ClientResponse response) {
			super(response.getEntity(String.class));
			this.errorCode = response.getStatus();
		}

		public int getErrorCode() {
			return errorCode;
		}

	}

}
