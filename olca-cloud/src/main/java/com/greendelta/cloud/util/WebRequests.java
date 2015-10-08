package com.greendelta.cloud.util;

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

	public static ClientResponse call(Type type, String url, String sessionId) throws WebRequestException {
		return call(type, url, sessionId, null);
	}

	public static ClientResponse call(Type type, String url, String sessionId, Object data) throws WebRequestException {
		log.info(Strings.concat("Calling ", url, " with type " + type.name()));
		Client client = Client.create();
		WebResource resource = client.resource(url);
		Builder responseBuilder = resource.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE);
		if (sessionId != null)
			responseBuilder.cookie(new Cookie("JSESSIONID", sessionId));
		if (data != null)
			responseBuilder.entity(new Gson().toJson(data), MediaType.APPLICATION_JSON_TYPE);
		ClientResponse response = null;
		switch (type) {
		case GET:
			response = responseBuilder.get(ClientResponse.class);
			break;
		case POST:
			response = responseBuilder.post(ClientResponse.class);
			break;
		case PUT:
			response = responseBuilder.put(ClientResponse.class);
			break;
		case DELETE:
			response = responseBuilder.delete(ClientResponse.class);
			break;
		}
		if (response.getStatus() >= 400 && response.getStatus() <= 599)
			throw new WebRequestException(response);
		return response;
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
